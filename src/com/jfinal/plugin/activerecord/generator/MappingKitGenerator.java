/**
 * Copyright (c) 2011-2016, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.plugin.activerecord.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.StrKit;

/**
 * MappingKit 文件生成器
 */
public class MappingKitGenerator {
	protected boolean generateMappingKit = true;
	protected String packageTemplate =
		"package %s;%n%n";
	protected String importTemplate =
		"import com.jfinal.plugin.activerecord.ActiveRecordPlugin;%n%n";
	protected String classDefineTemplate =
		"/**%n" +
		" * Generated by JFinal, do not modify this file.%n" +
		" * <pre>%n" +
		" * Example:%n" +
		" * public void configPlugin(Plugins me) {%n" +
		" *     ActiveRecordPlugin arp = new ActiveRecordPlugin(...);%n" +
		" *     %s.mapping(arp);%n" +
		" *     me.add(arp);%n" +
		" * }%n" +
		" * </pre>%n" +
		" */%n" +
		"public class %s {%n%n";
	protected String mappingMethodDefineTemplate =
			"\tpublic static void mapping(ActiveRecordPlugin arp) {%n";
	protected String mappingMethodContentTemplate =
				"\t\tarp.addMapping(\"%s\", \"%s\", %s.class);%n";
	protected String compositeKeyTemplate =
			"\t\t// Composite Primary Key order: %s%n";
	
	protected String mappingKitPackageName;
	protected String mappingKitOutputDir;
	protected String mappingKitClassName = "_MappingKit";
	
	public MappingKitGenerator(String mappingKitPackageName, String mappingKitOutputDir) {
		this.mappingKitPackageName = mappingKitPackageName;
		this.mappingKitOutputDir = mappingKitOutputDir;
	}
	
	public void setMappingKitOutputDir(String mappingKitOutputDir) {
		if (StrKit.notBlank(mappingKitOutputDir))
			this.mappingKitOutputDir = mappingKitOutputDir;
	}
	
	public void setMappingKitPackageName(String mappingKitPackageName) {
		if (StrKit.notBlank(mappingKitPackageName))
			this.mappingKitPackageName = mappingKitPackageName;
	}
	
	public void setMappingKitClassName(String mappingKitClassName) {
		if (StrKit.notBlank(mappingKitClassName))
			this.mappingKitClassName = StrKit.firstCharToUpperCase(mappingKitClassName);
	}
	
	public void setGenerateMappingKit(boolean generateMappingKit) {
		this.generateMappingKit = generateMappingKit;
	}

	public void generate(List<TableMeta> tableMetas) {
		if(generateMappingKit){
			System.out.println("Generate MappingKit file ...");
			StringBuilder ret = new StringBuilder();
			genPackage(ret);
			genImport(ret);
			genClassDefine(ret);
			genMappingMethod(tableMetas, ret);
			ret.append(String.format("}%n%n"));
			wirtToFile(ret);
		}
	}
	
	protected void genPackage(StringBuilder ret) {
		ret.append(String.format(packageTemplate, mappingKitPackageName));
	}
	
	protected void genImport(StringBuilder ret) {
		ret.append(String.format(importTemplate));
	}
	
	protected void genClassDefine(StringBuilder ret) {
		ret.append(String.format(classDefineTemplate, mappingKitClassName, mappingKitClassName));
	}
	
	protected void genMappingMethod(List<TableMeta> tableMetas, StringBuilder ret) {
		ret.append(String.format(mappingMethodDefineTemplate));
		for (TableMeta tableMeta : tableMetas) {
			boolean isCompositPrimaryKey = tableMeta.primaryKey.contains(",");
			if (isCompositPrimaryKey)
				ret.append(String.format(compositeKeyTemplate, tableMeta.primaryKey));
			String add = String.format(mappingMethodContentTemplate, tableMeta.name, tableMeta.primaryKey, tableMeta.modelName);
			ret.append(add);
		}
		ret.append(String.format("\t}%n"));
	}
	
	/**
	 * _MappingKit.java 覆盖写入
	 */
	protected void wirtToFile(StringBuilder ret) {
		FileWriter fw = null;
		try {
			File dir = new File(mappingKitOutputDir);
			if (!dir.exists())
				dir.mkdirs();
			
			String target = mappingKitOutputDir + File.separator + mappingKitClassName + ".java";
			fw = new FileWriter(target);
			fw.write(ret.toString());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (fw != null)
				try {fw.close();} catch (IOException e) {LogKit.error(e.getMessage(), e);}
		}
	}
}




