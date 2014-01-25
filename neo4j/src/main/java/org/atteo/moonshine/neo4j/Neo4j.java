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
package org.atteo.moonshine.neo4j;

import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atteo.evo.config.XmlDefaultValue;
import org.atteo.moonshine.blueprints.BlueprintsService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSetting;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;

/**
 * Neo4j Graph DB Service.
 */
@XmlRootElement(name = "neo4j")
public class Neo4j extends BlueprintsService {
	/***
	 * The type of the database persistence
	 *
	 * Can be embedded or impermanent
	 */
	@XmlElement
	@XmlDefaultValue("embedded")
	String type;

	/**
	 * The path where DB files are stored.
	 */
	@XmlElement
	@XmlDefaultValue("${dataHome}/neo4j")
	private String path;

	private GraphDatabaseBuilder builder;

	private class GraphDatabaseServiceProvider implements Provider<GraphDatabaseService> {
		@Override
		public GraphDatabaseService get() {
			checkNotNull(builder, "Expected a builder instance.");
			return builder.newGraphDatabase();
		}
	}

	private class BlueprintsGraphProvider implements Provider<Graph> {
		@Inject
		GraphDatabaseService graphDb;

		@Override
		public Graph get() {
			return new Neo4jGraph(graphDb);
		}
	}

	@Override
	public Module configure() {
		return new AbstractModule() {
			@Override
			protected void configure() {
				checkNotNull(type, "Type must be set.");

				if (type.equalsIgnoreCase("impermanent")) {
					builder = new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder();
				} else if (type.equals("embedded")) {
					checkNotNull(path, "Path must be specified.");
					builder = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(path);
				} else {
					throw new IllegalArgumentException("type must be impermanent/embedded.");
				}

				checkNotNull(builder, "Could not create a builder for the specified type [%s]", type);
				bind(GraphDatabaseService.class).toProvider(new GraphDatabaseServiceProvider()).in(Scopes.SINGLETON);
				bind(Graph.class).toProvider(new BlueprintsGraphProvider()).in(Scopes.SINGLETON);
			}
		};
	}

	// TODO: Support setting db settings in configuration
	public void settings(GraphDatabaseBuilder builder, Map<GraphDatabaseSetting, String> settings) {
		for (Map.Entry<GraphDatabaseSetting, String> entry : settings.entrySet()) {
			builder.setConfig(entry.getKey(), entry.getValue());
		}
	}
}
