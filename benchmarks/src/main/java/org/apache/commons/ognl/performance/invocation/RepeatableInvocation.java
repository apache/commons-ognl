/*
 * $Id$
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

import org.apache.commons.ognl.performance.objects.BaseBean;
import org.apache.commons.ognl.performance.objects.BaseGeneric;
import org.apache.commons.ognl.performance.objects.BaseIndexed;
import org.apache.commons.ognl.performance.objects.BaseObjectIndexed;
import org.apache.commons.ognl.performance.objects.BaseSyntheticObject;
import org.apache.commons.ognl.performance.objects.Bean1;
import org.apache.commons.ognl.performance.objects.Bean2;
import org.apache.commons.ognl.performance.objects.Bean3;
import org.apache.commons.ognl.performance.objects.BeanProvider;
import org.apache.commons.ognl.performance.objects.BeanProviderAccessor;
import org.apache.commons.ognl.performance.objects.BeanProviderImpl;
import org.apache.commons.ognl.performance.objects.Component;
import org.apache.commons.ognl.performance.objects.ComponentImpl;
import org.apache.commons.ognl.performance.objects.ComponentSubclass;
import org.apache.commons.ognl.performance.objects.Copy;
import org.apache.commons.ognl.performance.objects.CorrectedObject;
import org.apache.commons.ognl.performance.objects.Cracker;
import org.apache.commons.ognl.performance.objects.Entry;
import org.apache.commons.ognl.performance.objects.EvenOdd;
import org.apache.commons.ognl.performance.objects.FirstBean;
import org.apache.commons.ognl.performance.objects.FormComponentImpl;
import org.apache.commons.ognl.performance.objects.FormImpl;
import org.apache.commons.ognl.performance.objects.GameGeneric;
import org.apache.commons.ognl.performance.objects.GameGenericObject;
import org.apache.commons.ognl.performance.objects.GenericCracker;
import org.apache.commons.ognl.performance.objects.GenericObject;
import org.apache.commons.ognl.performance.objects.GenericRoot;
import org.apache.commons.ognl.performance.objects.GenericService;
import org.apache.commons.ognl.performance.objects.GenericServiceImpl;
import org.apache.commons.ognl.performance.objects.GetterMethods;
import org.apache.commons.ognl.performance.objects.IComponent;
import org.apache.commons.ognl.performance.objects.IContentProvider;
import org.apache.commons.ognl.performance.objects.IForm;
import org.apache.commons.ognl.performance.objects.IFormComponent;
import org.apache.commons.ognl.performance.objects.ITreeContentProvider;
import org.apache.commons.ognl.performance.objects.Indexed;
import org.apache.commons.ognl.performance.objects.IndexedMapObject;
import org.apache.commons.ognl.performance.objects.IndexedSetObject;
import org.apache.commons.ognl.performance.objects.Inherited;
import org.apache.commons.ognl.performance.objects.ListSource;
import org.apache.commons.ognl.performance.objects.ListSourceImpl;
import org.apache.commons.ognl.performance.objects.MenuItem;
import org.apache.commons.ognl.performance.objects.Messages;
import org.apache.commons.ognl.performance.objects.Model;
import org.apache.commons.ognl.performance.objects.MyMap;
import org.apache.commons.ognl.performance.objects.MyMapImpl;
import org.apache.commons.ognl.performance.objects.ObjectIndexed;
import org.apache.commons.ognl.performance.objects.OtherObjectIndexed;
import org.apache.commons.ognl.performance.objects.PersonGenericObject;
import org.apache.commons.ognl.performance.objects.PropertyHolder;
import org.apache.commons.ognl.performance.objects.Root;
import org.apache.commons.ognl.performance.objects.SearchCriteria;
import org.apache.commons.ognl.performance.objects.SearchTab;
import org.apache.commons.ognl.performance.objects.SecondBean;
import org.apache.commons.ognl.performance.objects.SetterReturns;
import org.apache.commons.ognl.performance.objects.Simple;
import org.apache.commons.ognl.performance.objects.SimpleEnum;
import org.apache.commons.ognl.performance.objects.SimpleNumeric;
import org.apache.commons.ognl.performance.objects.SubclassSyntheticObject;
import org.apache.commons.ognl.performance.objects.TestClass;
import org.apache.commons.ognl.performance.objects.TestImpl;
import org.apache.commons.ognl.performance.objects.TestInherited1;
import org.apache.commons.ognl.performance.objects.TestInherited2;
import org.apache.commons.ognl.performance.objects.TestModel;
import org.apache.commons.ognl.performance.objects.TreeContentProvider;
import org.apache.commons.ognl.performance.objects.Two;
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

    private int times=1000;

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
        invoke( );

    }

    public RepeatableInvocation( RuntimeWrapper runtimeWrapper, int times )
        throws Exception
    {

        this.runtimeWrapper = runtimeWrapper;
        this.times = times;
        invoke( );
    }

    private void invoke( )
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

    protected RuntimeWrapper getRuntime( )
    {
        return runtimeWrapper;
    }
}
