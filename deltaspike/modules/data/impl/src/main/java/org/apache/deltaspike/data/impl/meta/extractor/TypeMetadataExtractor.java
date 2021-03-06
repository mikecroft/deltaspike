/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.deltaspike.data.impl.meta.extractor;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.deltaspike.data.impl.meta.RepositoryEntity;
import org.apache.deltaspike.data.impl.util.EntityUtils;

public class TypeMetadataExtractor implements MetadataExtractor
{

    private static final Logger log = Logger.getLogger(TypeMetadataExtractor.class.getName());

    @Override
    public RepositoryEntity extract(Class<?> repoClass)
    {
        for (Type inf : repoClass.getGenericInterfaces())
        {
            RepositoryEntity result = extractFrom(inf);
            if (result != null)
            {
                return result;
            }
        }
        RepositoryEntity result = extractFrom(repoClass.getGenericSuperclass());
        if (result != null)
        {
            return result;
        }
        for (Type intf : repoClass.getGenericInterfaces())
        {
            result = extractFrom(intf);
            if (result != null)
            {
                return result;
            }
        }
        if (repoClass.getSuperclass() != null)
        {
            return extract(repoClass.getSuperclass());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private RepositoryEntity extractFrom(Type type)
    {
        log.log(Level.FINER, "extractFrom: type = {0}", type);
        if (!(type instanceof ParameterizedType))
        {
            return null;
        }
        
        ParameterizedType parametrizedType = (ParameterizedType) type;
        Type[] genericTypes = parametrizedType.getActualTypeArguments();
        
        RepositoryEntity result = null;
        
        // don't use a foreach here, we must be sure that the we first get the entity type
        for (int i = 0; i < genericTypes.length; i++)
        {
            Type genericType = genericTypes[i];
            
            if (genericType instanceof Class && EntityUtils.isEntityClass((Class<?>) genericType))
            {
                result = new RepositoryEntity((Class<?>) genericType);
                continue;
            }
            if (result != null && genericType instanceof Class)
            {
                result.setPrimaryKeyClass((Class<? extends Serializable>) genericType);
                return result;
            }
        }
        return result;
    }

}
