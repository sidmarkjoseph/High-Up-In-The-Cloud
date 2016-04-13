package com.vmware.spbm.connection.helpers.builders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.vmware.vim25.PropertySpec;

/**
 * a simple builder that creates a property spec
 */
public class PropertySpecBuilder extends PropertySpec {
   public PropertySpecBuilder addToPathSet(final Collection<String> paths) {
      init();
      this.pathSet.addAll(paths);
      return this;
   }

   public PropertySpecBuilder all(final Boolean all) {
      this.setAll(all);
      return this;
   }

   private void init() {
      if (pathSet == null) {
         pathSet = new ArrayList<String>();
      }
   }

   public PropertySpecBuilder pathSet(final String... paths) {
      init();
      this.pathSet.addAll(Arrays.asList(paths));
      return this;
   }

   public PropertySpecBuilder type(final String type) {
      this.setType(type);
      return this;
   }
}
