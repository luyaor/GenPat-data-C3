/*
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.tools.ant.types.resources;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.selectors.ResourceSelector;
import org.apache.tools.ant.types.resources.selectors.ResourceSelectorContainer;

/**
 * ResourceCollection that allows a number of selectors to be
 * applied to a single ResourceCollection for the purposes of
 * restricting or narrowing results.
 * @since Ant 1.7
 */
public class Restrict
    extends ResourceSelectorContainer implements ResourceCollection {

    private BaseResourceCollectionWrapper w = new BaseResourceCollectionWrapper() {
        /**
         * Restrict the nested ResourceCollection based on the nested selectors.
         * @return a Collection of Resources.
         */
        protected Collection getCollection() {
            ArrayList result = new ArrayList();
outer:      for (Iterator ri = w.getResourceCollection().iterator(); ri.hasNext();) {
                Resource r = (Resource) ri.next();
                for (Iterator i = getSelectors(); i.hasNext();) {
                    if (!((ResourceSelector) (i.next())).isSelected(r)) {
                        continue outer;
                    }
                }
                result.add(r);
            }
            return result;
        }

    };

    /**
     * Add the ResourceCollection.
     * @param c the ResourceCollection to add.
     */
    public synchronized void add(ResourceCollection c) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        w.add(c);
    }

    /**
     * Add a ResourceSelector.
     * @param s the ResourceSelector to add.
     */
    public synchronized void add(ResourceSelector s) {
        super.add(s);
        FailFast.invalidate(this);
    }

    /**
     * Fulfill the ResourceCollection contract.
     * @return an Iterator of Resources.
     */
    public final synchronized Iterator iterator() {
        if (isReference()) {
            return ((Restrict) getCheckedRef()).iterator();
        }
        dieOnCircularReference();
        return new FailFast(this, w.iterator());
    }

    /**
     * Fulfill the ResourceCollection contract.
     * @return number of elements as int.
     */
    public synchronized int size() {
        if (isReference()) {
            return ((Restrict) getCheckedRef()).size();
        }
        dieOnCircularReference();
        return w.size();
    }

    /**
     * Fulfill the ResourceCollection contract.
     * @return whether this is a filesystem-only resource collection.
     */
    public synchronized boolean isFilesystemOnly() {
        if (isReference()) {
            return ((Restrict) getCheckedRef()).isFilesystemOnly();
        }
        dieOnCircularReference();
        return w.isFilesystemOnly();
    }

}
