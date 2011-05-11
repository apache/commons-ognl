package org.apache.commons.ognl.test.objects;

/**
 *
 */
public interface GenericService {

    String getFullMessageFor(PersonGenericObject person, Object...arguments);

    String getFullMessageFor(GameGenericObject game, Object...arguments);    
}
