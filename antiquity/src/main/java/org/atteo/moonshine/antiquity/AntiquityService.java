/*
 * Contributed by Asaf Shakarchi <asaf000@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atteo.moonshine.antiquity;

import org.atteo.config.XmlDefaultValue;
import org.atteo.moonshine.blueprints.BlueprintsService;
import javax.xml.bind.annotation.XmlElement;

/**
 * Antiquity, a Blueprints DB abstraction service with historic support.
 * <p/>
 * <p>
 * Graph DB services that supports Blueprints API and also requires Historical
 * support should inherit from this service and bind {@link com.vertixtech.antiquity.graph.HistoricVersionedGraph}
 * and {@link com.vertixtech.antiquity.graph.ActiveVersionedGraph}
 * </p>
 */
public abstract class AntiquityService extends BlueprintsService {
    @XmlElement
    @XmlDefaultValue("true")
    protected Boolean privateVertexHash;

    @XmlElement
    @XmlDefaultValue("false")
    protected Boolean naturalIds;

    @XmlElement
    @XmlDefaultValue("true")
    protected Boolean naturalIdsOnlyIfSuppliedIdsAreIgnored;

    @XmlElement
    @XmlDefaultValue("true")
    protected Boolean dontVersionEmptyTransactions;

    @XmlElement
    @XmlDefaultValue("java.lang.Long")
    protected String identifierClassType;

    @XmlElement
    @XmlDefaultValue("false")
    protected Boolean init;
}
