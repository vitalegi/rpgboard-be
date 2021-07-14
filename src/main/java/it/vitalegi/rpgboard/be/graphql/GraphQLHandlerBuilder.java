package it.vitalegi.rpgboard.be.graphql;

import graphql.GraphQL;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.Future;
import io.vertx.ext.web.handler.graphql.schema.VertxDataFetcher;
import io.vertx.reactivex.core.Vertx;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

public class GraphQLHandlerBuilder {

  public GraphQL createGraphQL(Vertx vertx) {
    String schema = vertx.fileSystem().readFileBlocking("links.graphqls").toString();
    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

    RuntimeWiring runtimeWiring =
        newRuntimeWiring()
            .type(
                "Query",
                builder -> {
                  VertxDataFetcher<List<Link>> getAllLinks =
                      VertxDataFetcher.create(this::getAllLinks);
                  return builder.dataFetcher("allLinks", getAllLinks);
                })
            .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema =
        schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    return GraphQL.newGraphQL(graphQLSchema).build();
  }

  private Future<List<Link>> getAllLinks(DataFetchingEnvironment env) {
    boolean secureOnly = env.getArgument("secureOnly");
    List<Link> result =
        links().stream()
            .filter(link -> !secureOnly || link.getUrl().startsWith("https://"))
            .collect(Collectors.toList());
    return Future.succeededFuture(result);
  }

  private List<Link> links() {
    List<Link> links = new ArrayList<>();
    links.add(new Link("https://vertx.io", "Vert.x project", user("peter")));
    links.add(new Link("https://www.eclipse.org", "Eclipse Foundation", user("paul")));
    links.add(new Link("http://reactivex.io", "ReactiveX libraries", user("jack")));
    links.add(
        new Link("https://www.graphql-java.com", "GraphQL Java implementation", user("peter")));
    return links;
  }

  private User user(String name) {
    return new User(name);
  }
}
