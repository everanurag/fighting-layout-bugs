/*
 * Copyright 2009-2011 Michael Tamm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.fightinglayoutbugs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Base class for text detector implementations which provides the implementation
 * for the {@link TextDetector#ignore(String jQuerySelector)} method.
 *
 * @author Michael Tamm
 */
public abstract class AbstractTextDetector implements TextDetector {

    private List<String> jQuerySelectorsForElementsToIgnore;

    protected AbstractTextDetector() {
        jQuerySelectorsForElementsToIgnore = new ArrayList<String>();
        jQuerySelectorsForElementsToIgnore.add("applet");
        jQuerySelectorsForElementsToIgnore.add("embed");
        jQuerySelectorsForElementsToIgnore.add("iframe");
        jQuerySelectorsForElementsToIgnore.add("object");
    }

    @Override
    public void ignore(String jQuerySelector) {
        jQuerySelectorsForElementsToIgnore.add(jQuerySelector);
    }

    protected Collection<RectangularRegion> getIgnoredRegions(WebPage webPage) {
        return webPage.getRectangularRegionsCoveredBy(jQuerySelectorsForElementsToIgnore);
    }

}
