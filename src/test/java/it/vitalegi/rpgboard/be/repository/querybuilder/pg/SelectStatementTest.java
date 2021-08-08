package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class SelectStatementTest {
  Logger log = LoggerFactory.getLogger(this.getClass());

  PreparedStatementFactory factory1 =
      PreparedStatementFactory.init()
          .tableName("table1")
          .primaryKey("pk1")
          .primaryKey("pk2")
          .fields("pk1", "pk2", "field1", "field2")
          .autoGenerated("pk1");

  @Test
  void oneTableSomeFields() {
    assertEquals(
        "SELECT t1.pk2, t1.field1, t1.field2 FROM table1 as t1;",
        factory1.select().values().allExcept("pk1").build());
    assertEquals(
        "SELECT t1.pk2, t1.field1, t1.field2 FROM table1 as t1;",
        factory1.select().values().exact(Arrays.asList("pk2", "field1", "field2")).build());
  }

  @Test
  void oneTableNoPlaceholder() {
    assertEquals(
        "SELECT t1.pk1, t1.pk2, t1.field1, t1.field2 FROM table1 as t1;",
        factory1.select().build());
  }

  @Test
  void oneTableOnePlaceholder() {
    assertEquals(
        "SELECT t1.pk1, t1.pk2, t1.field1, t1.field2 FROM table1 as t1 WHERE t1.pk1=#{pk1};",
        factory1.select().where().isEqualsToPlaceholder("pk1").end().build());
  }

  @Test
  void oneTableTwoPlaceholder() {
    assertEquals(
        "SELECT t1.pk1, t1.pk2, t1.field1, t1.field2 FROM table1 as t1 WHERE t1.pk1=#{pk1} AND t1.pk2=#{pk2};",
        factory1.select().where().areEqualToPlaceholder(Arrays.asList("pk1", "pk2")).end().build());
  }
}
