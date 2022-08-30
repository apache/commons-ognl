/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.commons.ognl.performance.invocation;

import org.apache.commons.ognl.test.objects.BaseBean;
import org.apache.commons.ognl.test.objects.BaseGeneric;
import org.apache.commons.ognl.test.objects.BaseIndexed;
import org.apache.commons.ognl.test.objects.BaseObjectIndexed;
import org.apache.commons.ognl.test.objects.BaseSyntheticObject;
import org.apache.commons.ognl.test.objects.Bean1;
import org.apache.commons.ognl.test.objects.Bean2;
import org.apache.commons.ognl.test.objects.Bean3;
import org.apache.commons.ognl.test.objects.BeanProvider;
import org.apache.commons.ognl.test.objects.BeanProviderAccessor;
import org.apache.commons.ognl.test.objects.BeanProviderImpl;
import org.apache.commons.ognl.test.objects.Component;
import org.apache.commons.ognl.test.objects.ComponentImpl;
import org.apache.commons.ognl.test.objects.ComponentSubclass;
import org.apache.commons.ognl.test.objects.Copy;
import org.apache.commons.ognl.test.objects.CorrectedObject;
import org.apache.commons.ognl.test.objects.Cracker;
import org.apache.commons.ognl.test.objects.Entry;
import org.apache.commons.ognl.test.objects.EvenOdd;
import org.apache.commons.ognl.test.objects.FirstBean;
import org.apache.commons.ognl.test.objects.FormComponentImpl;
import org.apache.commons.ognl.test.objects.FormImpl;
import org.apache.commons.ognl.test.objects.GameGeneric;
import org.apache.commons.ognl.test.objects.GameGenericObject;
import org.apache.commons.ognl.test.objects.GenericCracker;
import org.apache.commons.ognl.test.objects.GenericObject;
import org.apache.commons.ognl.test.objects.GenericRoot;
import org.apache.commons.ognl.test.objects.GenericService;
import org.apache.commons.ognl.test.objects.GenericServiceImpl;
import org.apache.commons.ognl.test.objects.GetterMethods;
import org.apache.commons.ognl.test.objects.IComponent;
import org.apache.commons.ognl.test.objects.IContentProvider;
import org.apache.commons.ognl.test.objects.IForm;
import org.apache.commons.ognl.test.objects.IFormComponent;
import org.apache.commons.ognl.test.objects.ITreeContentProvider;
import org.apache.commons.ognl.test.objects.Indexed;
import org.apache.commons.ognl.test.objects.IndexedMapObject;
import org.apache.commons.ognl.test.objects.IndexedSetObject;
import org.apache.commons.ognl.test.objects.Inherited;
import org.apache.commons.ognl.test.objects.ListSource;
import org.apache.commons.ognl.test.objects.ListSourceImpl;
import org.apache.commons.ognl.test.objects.MenuItem;
import org.apache.commons.ognl.test.objects.Messages;
import org.apache.commons.ognl.test.objects.Model;
import org.apache.commons.ognl.test.objects.MyMap;
import org.apache.commons.ognl.test.objects.MyMapImpl;
import org.apache.commons.ognl.test.objects.ObjectIndexed;
import org.apache.commons.ognl.test.objects.OtherObjectIndexed;
import org.apache.commons.ognl.test.objects.PersonGenericObject;
import org.apache.commons.ognl.test.objects.PropertyHolder;
import org.apache.commons.ognl.test.objects.Root;
import org.apache.commons.ognl.test.objects.SearchCriteria;
import org.apache.commons.ognl.test.objects.SearchTab;
import org.apache.commons.ognl.test.objects.SecondBean;
import org.apache.commons.ognl.test.objects.SetterReturns;
import org.apache.commons.ognl.test.objects.Simple;
import org.apache.commons.ognl.test.objects.SimpleEnum;
import org.apache.commons.ognl.test.objects.SimpleNumeric;
import org.apache.commons.ognl.test.objects.SubclassSyntheticObject;
import org.apache.commons.ognl.test.objects.TestClass;
import org.apache.commons.ognl.test.objects.TestImpl;
import org.apache.commons.ognl.test.objects.TestInherited1;
import org.apache.commons.ognl.test.objects.TestInherited2;
import org.apache.commons.ognl.test.objects.TestModel;
import org.apache.commons.ognl.test.objects.TreeContentProvider;
import org.apache.commons.ognl.test.objects.Two;
import org.apache.commons.ognl.performance.runtime.RuntimeWrapper;

import java.util.Arrays;
import java.util.List;

/**
 * User: Maurizio Cucchiara
 * Date: 10/22/11
 * Time: 12:20 AM
 */
public abstract class RepeatableInvocation
{
    private RuntimeWrapper runtimeWrapper;

    private int times = 1000;

    private List<Class<?>> classes =
        Arrays.asList( ComponentImpl.class, BaseObjectIndexed.class, TestInherited2.class, MenuItem.class,
                       BaseIndexed.class, ListSourceImpl.class, GenericService.class, Copy.class, Inherited.class,
                       MyMapImpl.class, GenericCracker.class, MyMap.class, SecondBean.class, SetterReturns.class,
                       IContentProvider.class, FirstBean.class, CorrectedObject.class, BeanProviderImpl.class,
                       TestClass.class, TestImpl.class, TreeContentProvider.class, Messages.class, Two.class,
                       IndexedMapObject.class, SimpleNumeric.class, GameGeneric.class, Entry.class,
                       SubclassSyntheticObject.class, SimpleEnum.class, Model.class, Simple.class,
                       BaseSyntheticObject.class, ObjectIndexed.class, IComponent.class, SearchCriteria.class,
                       FormImpl.class, TestInherited1.class, IndexedSetObject.class, SearchTab.class,
                       GenericObject.class, GenericRoot.class, Bean1.class, FormComponentImpl.class,
                       ComponentSubclass.class, IForm.class, OtherObjectIndexed.class, Cracker.class, Indexed.class,
                       EvenOdd.class, PersonGenericObject.class, Bean2.class, GenericServiceImpl.class,
                       IFormComponent.class, ListSource.class, PropertyHolder.class, Bean3.class, BaseGeneric.class,
                       GetterMethods.class, BeanProviderAccessor.class, BeanProvider.class, GameGenericObject.class,
                       ITreeContentProvider.class, TestModel.class, BaseBean.class, Component.class, Root.class );

    public RepeatableInvocation( RuntimeWrapper runtimeWrapper )
        throws Exception
    {
        this.runtimeWrapper = runtimeWrapper;
        invoke();

    }

    public RepeatableInvocation( RuntimeWrapper runtimeWrapper, int times )
        throws Exception
    {

        this.runtimeWrapper = runtimeWrapper;
        this.times = times;
        invoke();
    }

    private void invoke()
        throws Exception
    {
        for ( int i = 0; i < times; i++ )
        {
            for ( Class<?> c : classes )
            {
                invoke( c );
            }
        }
    }

    protected abstract void invoke( Class<?> c )
        throws Exception;

    protected RuntimeWrapper getRuntime()
    {
        return runtimeWrapper;
    }
}
