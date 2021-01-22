package edu.uoc.som.temf.core;

import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.eclipse.emf.ecore.EStructuralFeature;

public final class TGlobalClock {

	public static final Clock INSTANCE = new NonRepeatingClock(ZoneOffset.UTC);
	
	private TGlobalClock() {
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
	private static final class NonRepeatingClock extends Clock implements Serializable {
		
		private static final long serialVersionUID = 1L;
		private final Clock clock;
		private Instant lastInstant = Instant.MIN;

		private NonRepeatingClock(ZoneId zone) {
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
}
