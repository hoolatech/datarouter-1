/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.model.field;

import java.lang.reflect.Type;
import java.util.Optional;

import io.datarouter.model.field.encoding.FieldGeneratorType;

/**
 * FieldKey is a mapping from a java field to the datastore. When using an RDBMS, FieldKey defines the column name,
 * datatype, and metadata.
 *
 * The getName() method refers to the java field's name. This name is used to choose the default datastore column name,
 * but the datastore column name can be overridden with getColumnName().
 *
 * FieldKeys are immutable so may be defined as constants and reused across many Field objects.
 */
public interface FieldKey<T>{

	String getName();

	String getColumnName();
	byte[] getColumnNameBytes();

	boolean isNullable();

	Type getValueType();

	FieldGeneratorType getAutoGeneratedType();
	T generateRandomValue();

	boolean isFixedLength();

	boolean isCollection();//this is used by viewNodeData.jsp to add line breaks to the table
	T getDefaultValue();

	Field<T> createValueField(T value);

	<U extends FieldKeyAttribute<U>> Optional<U> findAttribute(FieldKeyAttributeKey<U> key);

}
