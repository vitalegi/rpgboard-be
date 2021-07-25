package it.vitalegi.rpgboard.be.graphql;

import graphql.GraphQL;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.reactivex.Flowable;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.ext.web.handler.graphql.schema.VertxDataFetcher;
import io.vertx.reactivex.RxHelper;
import io.vertx.reactivex.core.Vertx;
import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

public class ApolloWSHandlerBuilder {

  public GraphQL createGraphQL(Vertx vertx, Context context) {
    String schema = vertx.fileSystem().readFileBlocking("links.graphqls").toString();
    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

    RuntimeWiring runtimeWiring =
        newRuntimeWiring()
            .type(
                "Subscription",
                builder -> builder.dataFetcher("links", env -> linksFetcher(env, context)))
            .build();

    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema =
        schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);

    return GraphQL.newGraphQL(graphQLSchema).build();
  }

  private Publisher<Link> linksFetcher(DataFetchingEnvironment env, Context context) {
    return Flowable.interval(1, TimeUnit.SECONDS) // Ticks
        .zipWith(
            Flowable.fromIterable(GraphQLHandlerBuilder.LINKS),
            (tick, link) -> {
              System.out.println("> " + tick + " - " + link);
              return link;
            }) // Emit link on each tick
        .observeOn(RxHelper.scheduler(context)); // Observe on the verticle context thread
  }

  private static User user(String name) {
    return new User(name);
  }
}
