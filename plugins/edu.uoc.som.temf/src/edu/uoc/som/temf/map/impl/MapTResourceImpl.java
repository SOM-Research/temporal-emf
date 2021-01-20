/*******************************************************************************
 * Copyright (c) 2018 Abel Gómez.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Abel Gómez - initial API and implementation
 *******************************************************************************/
package edu.uoc.som.temf.map.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.AbstractTreeIterator;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.InternalEObject.EStore;
import org.eclipse.emf.ecore.impl.EClassifierImpl;
import org.eclipse.emf.ecore.impl.EReferenceImpl;
import org.eclipse.emf.ecore.impl.EStoreEObjectImpl;
import org.eclipse.emf.ecore.impl.EStoreEObjectImpl.EStoreEList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.osgi.util.NLS;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.OffHeapStore;

import edu.uoc.som.temf.Logger;
import edu.uoc.som.temf.TURI;
import edu.uoc.som.temf.core.InternalTObject;
import edu.uoc.som.temf.core.TObject;
import edu.uoc.som.temf.core.TResource;
import edu.uoc.som.temf.core.impl.TObjectAdapterFactoryImpl;
import edu.uoc.som.temf.core.impl.TObjectImpl;
import edu.uoc.som.temf.estores.SearcheableResourceTStore;
import edu.uoc.som.temf.estores.TStore;
import edu.uoc.som.temf.estores.impl.IsSetCachingDelegatedTStoreImpl;
import edu.uoc.som.temf.estores.impl.SizeCachingDelegatedTStoreImpl;

public class MapTResourceImpl extends ResourceImpl implements TResource {

	protected static final ResourceContentsEStructuralFeature ROOT_CONTENTS_ESTRUCTURALFEATURE = new ResourceContentsEStructuralFeature();

	protected final DummyRootEObject DUMMY_ROOT_EOBJECT = new DummyRootEObject(this);

	protected SearcheableResourceTStore tStore;

	protected MVStore mvStore;

	protected boolean isPersistent = false;

	protected Clock clock = new NonRepeatingClock(ZoneOffset.UTC);

	public MapTResourceImpl(URI uri) {
		super(uri);
		this.mvStore = new MVStore.Builder().fileStore(new OffHeapStore()).open();
		this.tStore = new DirectWriteMapResourceTStoreImpl(this, mvStore);
		this.isPersistent = false;
	}

	@Override
	public void load(Map<?, ?> options) throws IOException {
		try {
			isLoading = true;
			if (isLoaded) {
				return;
			} else if (!getFile().exists()) {
				throw new FileNotFoundException(uri.toFileString());
			} else {
				this.mvStore = MVStore.open(getFile().getAbsolutePath());
				this.isPersistent = true;
				this.tStore = createResourceEStore(this.mvStore);
			}
			isLoaded = true;
		} finally {
			isLoading = false;
		}
	}

	@Override
	public void save(Map<?, ?> options) throws IOException {
		if (!isLoaded()) {
			// If a resource is unloaded it's because no contents have been set (or no file
			// has been read from disk), thus, a save in an unloaded Resource does not make 
			// sense since it's empty. We do nothing in order to be compliant with the EMF API.
			return;
		} else if (!this.isPersistent) {
			if (!getFile().getParentFile().exists()) {
				getFile().getParentFile().mkdirs();
			}
			MVStore newMvStore = MVStore.open(getFile().getAbsolutePath());
			if (!newMvStore.getMapNames().isEmpty()) {
				Logger.log(Logger.SEVERITY_WARNING,
						NLS.bind("Saving on existing store {0} without previously loading its contents. Contents will be lost.", getFile().toString()));
				newMvStore.getMapNames().forEach(name -> newMvStore.openMap(name).clear());
			}
			mvStore.getMapNames().forEach(name -> newMvStore.openMap(name).putAll(mvStore.openMap(name)));
			this.mvStore = newMvStore;
			this.isPersistent = true;
			this.tStore = createResourceEStore(this.mvStore);
			this.isLoaded = true;
		}

		mvStore.commit();
	}

	protected File getFile() {
		return FileUtils.getFile(TURI.createTURI(getURI().appendSegment("mvstore")).toFileString());
	}

	@Override
	public TStore tStore() {
		return tStore;
	}

	@Override
	public EList<EObject> getContents() {
		return new ResourceContentsEStoreEList(DUMMY_ROOT_EOBJECT, ROOT_CONTENTS_ESTRUCTURALFEATURE, tStore);
	}

	@SuppressWarnings("unchecked")
	@Override
	public EList<EObject> getContentsAt(Instant instant) {
		return ECollections.unmodifiableEList(
				(EList<EObject>) (Object) ECollections.asEList(tStore.toArrayAt(instant, DUMMY_ROOT_EOBJECT, ROOT_CONTENTS_ESTRUCTURALFEATURE)));
	}

	public TreeIterator<EObject> getAllContentsAt(final Instant instant) {
		return new AbstractTreeIterator<EObject>(this, false) {
			private static final long serialVersionUID = 1L;

			@Override
			public Iterator<EObject> getChildren(Object object) {
				return object == MapTResourceImpl.this ? MapTResourceImpl.this.getContentsAt(instant).iterator()
						: ((TObject) object).eContentsAt(instant).iterator();
			}
		};
	}

	@Override
	public EObject getEObject(String uriFragment) {
		EObject eObject = tStore.getEObject(uriFragment);
		if (eObject != null) {
			return eObject;
		} else {
			return super.getEObject(uriFragment);
		}
	}

	@Override
	public String getURIFragment(EObject eObject) {
		if (eObject.eResource() != this) {
			return "/-1";
		} else {
			// Try to adapt as a TObject and return the ID
			TObject tObject = TObjectAdapterFactoryImpl.getAdapter(eObject, TObject.class);
			if (tObject != null) {
				return (tObject.tId());
			}
		}
		return super.getURIFragment(eObject);
	}

	public static void shutdownWithoutUnload(MapTResourceImpl resource) {
		resource.shutdown();
	}

	protected void shutdown() {
		this.mvStore.close();
		this.mvStore = new MVStore.Builder().fileStore(new OffHeapStore()).open();
		this.tStore = new DirectWriteMapResourceTStoreImpl(this, mvStore);
		this.isPersistent = false;
	}

	@Override
	protected void doUnload() {
		Iterator<EObject> allContents = getAllProperContents(unloadingContents);
		getErrors().clear();
		getWarnings().clear();
		while (allContents.hasNext()) {
			unloaded((InternalEObject) allContents.next());
		}
		shutdown();
	}

	@Override
	protected void finalize() throws Throwable {
		unload();
	}

	protected SearcheableResourceTStore createResourceEStore(MVStore mvStore) throws IOException {
		return new IsSetCachingDelegatedTStoreImpl(new SizeCachingDelegatedTStoreImpl(new DirectWriteMapResourceTStoreImpl(this, mvStore)));
	}

	@Override
	public Clock getClock() {
		return clock;
	}

	/**
	 * Instant creation is key for identifying the moment when a
	 * {@link EStructuralFeature} is set.
	 * 
	 * However, although {@link Instant}'s precision is in the order of nanoseconds,
	 * it's accuracy is usually lower (even in the order of a few hundreds of
	 * nanoseconds). That means that multiple instants created in a row may be
	 * virtually the same {@link Instant}. To avoid duplicate keys in the map (and
	 * thus missing values in the history), we increment the instant in 1 nanosecond
	 * if the value is previous or the same than the last call to get an
	 * {@link Instant}. 1 ns is the period of a frequency of 1GHz, so hopefully, the
	 * number of subsequent calls to get {@link Instant}s will be low enough to
	 * avoid a big error accumulation in nowadays processors.
	 * 
	 * @author agomez
	 *
	 */
	static final class NonRepeatingClock extends Clock implements Serializable {
		private static final long serialVersionUID = 1L;
		private final Clock clock;
		private Instant lastInstant = Instant.MIN;

		NonRepeatingClock(ZoneId zone) {
			this.clock = Clock.system(zone);
		}

		@Override
		public ZoneId getZone() {
			return clock.getZone();
		}

		@Override
		public Clock withZone(ZoneId zone) {
			if (zone.equals(getZone())) {
				return this;
			}
			return new NonRepeatingClock(zone);
		}

		@Override
		public long millis() {
			return clock.millis();
		}

		@Override
		public Instant instant() {
			synchronized (lastInstant) {
				Instant readInstant = clock.instant();
				while (readInstant.isBefore(lastInstant) || readInstant.equals(lastInstant)) {
					readInstant = readInstant.plusNanos(1);
				}
				lastInstant = readInstant;
			}
			return lastInstant;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof NonRepeatingClock) {
				return clock.equals(((NonRepeatingClock) obj).clock);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return clock.hashCode() + 1;
		}

		@Override
		public String toString() {
			return "NonRepeatingClock[" + clock.getZone() + "]";
		}
	}

	/**
	 * Fake {@link EStructuralFeature} that represents the
	 * {@link Resource#getContents()} feature.
	 * 
	 * @author agomez
	 * 
	 */
	protected static class ResourceContentsEStructuralFeature extends EReferenceImpl {
		protected static final String RESOURCE__CONTENTS__FEATURE_NAME = "eContents";

		public ResourceContentsEStructuralFeature() {
			// @formatter:off
			this.setUpperBound(ETypedElement.UNBOUNDED_MULTIPLICITY);
			this.setLowerBound(0);
			this.setName(RESOURCE__CONTENTS__FEATURE_NAME);
			this.setEType(new EClassifierImpl() {});
			this.setFeatureID(RESOURCE__CONTENTS);
			// @formatter:on
		}
	}

	/**
	 * Dummy {@link EObject} that represents the root entry point for this
	 * {@link Resource}
	 * 
	 * @author agomez
	 * 
	 */
	protected final class DummyRootEObject extends TObjectImpl {
		protected static final String ROOT_EOBJECT_ID = "ROOT";

		public DummyRootEObject(Resource.Internal resource) {
			super();
			this.id = ROOT_EOBJECT_ID;
			eSetDirectResource(resource);
		}
	}

	/**
	 * A notifying {@link EStoreEList} list implementation for supporting
	 * {@link Resource#getContents}.
	 * 
	 * @author agomez
	 * 
	 */
	protected class ResourceContentsEStoreEList extends EStoreEObjectImpl.EStoreEList<EObject> {
		protected static final long serialVersionUID = 1L;

		protected ResourceContentsEStoreEList(InternalEObject owner, EStructuralFeature eStructuralFeature, EStore store) {
			super(owner, eStructuralFeature, store);
		}

		@Override
		protected EObject validate(int index, EObject object) {
			if (!canContainNull() && object == null) {
				throw new IllegalArgumentException("The 'no null' constraint is violated");
			}
			return object;
		}

		@Override
		public Object getNotifier() {
			return MapTResourceImpl.this;
		}

		@Override
		public int getFeatureID() {
			return RESOURCE__CONTENTS;
		}

		@Override
		protected boolean isNotificationRequired() {
			return MapTResourceImpl.this.eNotificationRequired();
		}

		@Override
		protected boolean useEquals() {
			return false;
		}

		@Override
		protected boolean hasInverse() {
			return true;
		}

		@Override
		protected boolean isUnique() {
			return true;
		}

		@Override
		public NotificationChain inverseAdd(EObject object, NotificationChain notifications) {
			InternalEObject eObject = (InternalEObject) object;
			notifications = eObject.eSetResource(MapTResourceImpl.this, notifications);
			MapTResourceImpl.this.attached(eObject);
			return notifications;
		}

		@Override
		public NotificationChain inverseRemove(EObject object, NotificationChain notifications) {
			InternalEObject eObject = (InternalEObject) object;
			if (MapTResourceImpl.this.isLoaded || unloadingContents != null) {
				MapTResourceImpl.this.detached(eObject);
			}
			return eObject.eSetResource(null, notifications);
		}

		@Override
		protected void delegateAdd(int index, EObject object) {
			// Maintain a list of hard links to the elements while moving
			// them to the new resource. If a garbage collection happens while
			// traversing the children elements, some unsaved objects that are
			// referenced from a saved object may be garbage collected before
			// they have been completely stored in the DB
			List<EObject> hardLinksList = new ArrayList<>();
			InternalTObject eObject = TObjectAdapterFactoryImpl.getAdapter(object, InternalTObject.class);
			// Collect all contents
			hardLinksList.add(object);
			eObject.eAllContents().forEachRemaining(e -> hardLinksList.add(e));
			// Iterate using the hard links list instead eAllContents
			hardLinksList.forEach(e -> TObjectAdapterFactoryImpl.getAdapter(e, InternalTObject.class).tSetResource(MapTResourceImpl.this));
			super.delegateAdd(index, object);
		}

		@Override
		protected EObject delegateRemove(int index) {
			// See comment on #delegateAdd about hardLinkList
			EObject object = super.delegateRemove(index);
			List<EObject> hardLinksList = new ArrayList<>();
			InternalTObject eObject = TObjectAdapterFactoryImpl.getAdapter(object, InternalTObject.class);
			hardLinksList.add(object);
			eObject.eAllContents().forEachRemaining(e -> hardLinksList.add(e));
			hardLinksList.forEach(e -> TObjectAdapterFactoryImpl.getAdapter(e, InternalTObject.class).tSetResource(null));
			return object;
		}

		@Override
		protected void didAdd(int index, EObject object) {
			super.didAdd(index, object);
			if (index == size() - 1) {
				loaded();
			}
			modified();
		}

		@Override
		protected void didRemove(int index, EObject object) {
			super.didRemove(index, object);
			modified();
		}

		@Override
		protected void didSet(int index, EObject newObject, EObject oldObject) {
			super.didSet(index, newObject, oldObject);
			modified();
		}

		@Override
		protected void didClear(int oldSize, Object[] oldData) {
			if (oldSize == 0) {
				loaded();
			} else {
				super.didClear(oldSize, oldData);
			}
		}

		protected void loaded() {
			if (!MapTResourceImpl.this.isLoaded()) {
				Notification notification = MapTResourceImpl.this.setLoaded(true);
				if (notification != null) {
					MapTResourceImpl.this.eNotify(notification);
				}
			}
		}

		protected void modified() {
			if (isTrackingModification()) {
				setModified(true);
			}
		}
	}
}
