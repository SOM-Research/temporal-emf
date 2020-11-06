package org.eclipse.emf.codegen.ecore.templates.model;

import java.util.*;
import org.eclipse.emf.codegen.ecore.genmodel.*;

public class ManifestMF
{
  protected static String nl;
  public static synchronized ManifestMF create(String lineSeparator)
  {
    nl = lineSeparator;
    ManifestMF result = new ManifestMF();
    nl = null;
    return result;
  }

  public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = "Manifest-Version: 1.0" + NL + "Bundle-ManifestVersion: 2" + NL + "Bundle-Name: ";
  protected final String TEXT_2 = NL + "Bundle-SymbolicName: ";
  protected final String TEXT_3 = ";singleton:=true" + NL + "Automatic-Module-Name: ";
  protected final String TEXT_4 = NL + "Bundle-Version: 1.0.0.qualifier" + NL + "Bundle-ClassPath: ";
  protected final String TEXT_5 = ".jar";
  protected final String TEXT_6 = ".";
  protected final String TEXT_7 = NL + "Bundle-Activator: ";
  protected final String TEXT_8 = "$Implementation";
  protected final String TEXT_9 = "$Activator";
  protected final String TEXT_10 = NL + "Bundle-Vendor: ";
  protected final String TEXT_11 = NL + "Bundle-Localization: ";
  protected final String TEXT_12 = NL + "Bundle-RequiredExecutionEnvironment: J2SE-1.5";
  protected final String TEXT_13 = NL + "Bundle-RequiredExecutionEnvironment: JavaSE-1.6";
  protected final String TEXT_14 = NL + "Bundle-RequiredExecutionEnvironment: JavaSE-1.7";
  protected final String TEXT_15 = NL + "Bundle-RequiredExecutionEnvironment: JavaSE-1.8";
  protected final String TEXT_16 = NL + "Bundle-RequiredExecutionEnvironment: JavaSE-9";
  protected final String TEXT_17 = NL + "Bundle-RequiredExecutionEnvironment: JavaSE-10";
  protected final String TEXT_18 = NL + "Bundle-RequiredExecutionEnvironment: JavaSE-11";
  protected final String TEXT_19 = NL + "Export-Package: ";
  protected final String TEXT_20 = ",";
  protected final String TEXT_21 = NL + " ";
  protected final String TEXT_22 = NL + "Require-Bundle: ";
  protected final String TEXT_23 = ";resolution:=optional;x-installation:=greedy";
  protected final String TEXT_24 = ";visibility:=reexport";
  protected final String TEXT_25 = NL + "Import-Package: org.osgi.framework";
  protected final String TEXT_26 = NL + "Eclipse-LazyStart: true";
  protected final String TEXT_27 = NL + "Bundle-ActivationPolicy: lazy";
  protected final String TEXT_28 = NL;

  public String generate(Object argument)
  {
    final StringBuffer stringBuffer = new StringBuffer();
    
/**
 * Copyright (c) 2005-2007 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: 
 *   IBM - Initial API and implementation
 */

    GenModel genModel = (GenModel)argument;
    stringBuffer.append(TEXT_1);
    stringBuffer.append(genModel.getModelBundleNameKey());
    stringBuffer.append(TEXT_2);
    stringBuffer.append(genModel.getModelPluginID());
    stringBuffer.append(TEXT_3);
    stringBuffer.append(genModel.getModelPluginID());
    stringBuffer.append(TEXT_4);
    if (genModel.isRuntimeJar()) {
    stringBuffer.append(genModel.getModelPluginID());
    stringBuffer.append(TEXT_5);
    }else{
    stringBuffer.append(TEXT_6);
    }
    if (genModel.hasModelPluginClass()) {
    stringBuffer.append(TEXT_7);
    stringBuffer.append(genModel.getQualifiedModelPluginClassName());
    stringBuffer.append(TEXT_8);
    if (genModel.isOSGiCompatible()) {
    stringBuffer.append(TEXT_9);
    }
    }
    stringBuffer.append(TEXT_10);
    stringBuffer.append(genModel.getModelBundleVendorKey());
    stringBuffer.append(TEXT_11);
    stringBuffer.append(genModel.getModelBundleLocalization());
    if (genModel.getComplianceLevel() == GenJDKLevel.JDK50_LITERAL) {
    stringBuffer.append(TEXT_12);
    } else if (genModel.getComplianceLevel() == GenJDKLevel.JDK60_LITERAL) {
    stringBuffer.append(TEXT_13);
    } else if (genModel.getComplianceLevel() == GenJDKLevel.JDK70_LITERAL) {
    stringBuffer.append(TEXT_14);
    } else if (genModel.getComplianceLevel() == GenJDKLevel.JDK80_LITERAL) {
    stringBuffer.append(TEXT_15);
    } else if (genModel.getComplianceLevel() == GenJDKLevel.JDK90_LITERAL) {
    stringBuffer.append(TEXT_16);
    } else if (genModel.getComplianceLevel() == GenJDKLevel.JDK100_LITERAL) {
    stringBuffer.append(TEXT_17);
    } else if (genModel.getComplianceLevel() == GenJDKLevel.JDK110_LITERAL) {
    stringBuffer.append(TEXT_18);
    }
    Iterator<String> packagesIterator = genModel.getModelQualifiedPackageNames().iterator(); if (packagesIterator.hasNext()) { String pack = packagesIterator.next();
    stringBuffer.append(TEXT_19);
    stringBuffer.append(pack);
    while(packagesIterator.hasNext()) { pack = packagesIterator.next();
    stringBuffer.append(TEXT_20);
    stringBuffer.append(TEXT_21);
    stringBuffer.append(pack);
    }
    }
    Iterator<String> requiredPluginIterator = genModel.getModelRequiredPlugins().iterator(); if (requiredPluginIterator.hasNext()) { String pluginID = requiredPluginIterator.next();
    stringBuffer.append(TEXT_22);
    stringBuffer.append(pluginID);
    if (pluginID.startsWith("org.eclipse.core.runtime")) {if (genModel.isOSGiCompatible()) {
    stringBuffer.append(TEXT_23);
    }} else {
    stringBuffer.append(TEXT_24);
    } while(requiredPluginIterator.hasNext()) { pluginID = requiredPluginIterator.next();
    stringBuffer.append(TEXT_20);
    stringBuffer.append(TEXT_21);
    stringBuffer.append(pluginID);
    if (pluginID.startsWith("org.eclipse.core.runtime")) {if (genModel.isOSGiCompatible()) {
    stringBuffer.append(TEXT_23);
    }} else if (!pluginID.equals("org.eclipse.xtext.xbase.lib") && !pluginID.equals("org.eclipse.emf.ecore.xcore.lib")) {
    stringBuffer.append(TEXT_24);
    }}
    }
    if (genModel.isOSGiCompatible() && genModel.hasModelPluginClass()) {
    stringBuffer.append(TEXT_25);
    }
    if (genModel.getRuntimeVersion() == GenRuntimeVersion.EMF22 || genModel.getRuntimeVersion() == GenRuntimeVersion.EMF23) {
    stringBuffer.append(TEXT_26);
    }
    stringBuffer.append(TEXT_27);
    stringBuffer.append(TEXT_28);
    return stringBuffer.toString();
  }
}
