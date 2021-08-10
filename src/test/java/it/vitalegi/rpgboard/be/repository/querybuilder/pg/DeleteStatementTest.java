package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import io.vertx.junit5.VertxExtension;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.FieldsPicker;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.returning.ReturningClause;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.where.EqualsPlaceholder;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.where.WhereClause;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class DeleteStatementTest {
  Logger log = LoggerFactory.getLogger(this.getClass());

  Table table1 =
      TableFactory.init()
          .tableName("table1")
          .primaryKey("pk1")
          .primaryKey("pk2")
          .fields("pk1", "pk2", "field1", "field2")
          .autoGenerated("pk1")
          .build();

  @Test
  void oneTableSomeFields() {
    assertEquals(
        "DELETE FROM table1 RETURNING pk1, field1;",
        DeleteFactory.init(table1)
            .returning(new ReturningClause(FieldsPicker.exact(Arrays.asList("pk1", "field1"))))
            .build());
  }

  @Test
  void oneTableNoPlaceholder() {
    assertEquals(
        "DELETE FROM table1 RETURNING pk1, pk2, field1, field2;",
        DeleteFactory.init(table1).build());
  }

  @Test
  void oneTableOnePlaceholder() {
    assertEquals(
        "DELETE FROM table1 WHERE (pk1=#{pk1}) RETURNING pk1, pk2, field1, field2;",
        DeleteFactory.init(table1)
            .where(WhereClause.and(new EqualsPlaceholder(FieldsPicker.exact("pk1"))))
            .build());
  }

  @Test
  void oneTableTwoPlaceholder() {
    assertEquals(
        "DELETE FROM table1 WHERE (pk1=#{pk1} AND pk2=#{pk2}) RETURNING pk1, pk2, field1, field2;",
        DeleteFactory.init(table1)
            .where(
                WhereClause.and(
                    new EqualsPlaceholder(FieldsPicker.exact(Arrays.asList("pk1", "pk2")))))
            .build());
  }
}
