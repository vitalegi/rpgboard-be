package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import io.vertx.junit5.VertxExtension;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.FieldsPicker;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.returning.ReturningClause;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.updateset.SetClause;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.where.EqualsPlaceholder;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.where.WhereClause;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class UpdateStatementTest {
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
        "UPDATE table1 SET pk2=#{pk2}, field1=#{field1}, field2=#{field2} RETURNING pk1, pk2, field1, field2;",
        UpdateFactory.init(table1)
            .set(SetClause.init().exact(null, Arrays.asList("pk2", "field1", "field2")))
            .build());
    assertEquals(
        "UPDATE table1 SET pk2=#{pk2}, field1=#{field1}, field2=#{field2} RETURNING pk1, field1;",
        UpdateFactory.init(table1)
            .set(SetClause.init().exact(null, Arrays.asList("pk2", "field1", "field2")))
            .returning(new ReturningClause(FieldsPicker.exact(Arrays.asList("pk1", "field1"))))
            .build());
    assertEquals(
        "UPDATE table1 SET pk2=#{pk2}, field1=#{field1}, field2=#{field2} RETURNING pk1, field1;",
        UpdateFactory.init(table1)
            .set(SetClause.init().exact(null, Arrays.asList("pk2", "field1", "field2")))
            .returning(new ReturningClause(FieldsPicker.except(Arrays.asList("pk2", "field2"))))
            .build());
  }

  @Test
  void oneTableNoPlaceholder() {
    assertEquals(
        "UPDATE table1 SET pk1=#{pk1}, pk2=#{pk2}, field1=#{field1}, field2=#{field2} RETURNING pk1, pk2, field1, field2;",
        UpdateFactory.init(table1).set(SetClause.init().all(null)).build());
  }

  @Test
  void oneTableOnePlaceholder() {
    assertEquals(
        "UPDATE table1 SET pk1=#{pk1}, pk2=#{pk2}, field1=#{field1}, field2=#{field2} WHERE (pk1=#{pk1}) RETURNING pk1, pk2, field1, field2;",
        UpdateFactory.init(table1)
            .set(SetClause.init().all(null))
            .where(
                WhereClause.and(
                    new EqualsPlaceholder(
                        FieldsPicker.exact(null, Collections.singletonList("pk1")))))
            .build());
  }

  @Test
  void oneTableTwoPlaceholder() {
    assertEquals(
        "UPDATE table1 SET pk1=#{pk1}, pk2=#{pk2}, field1=#{field1}, field2=#{field2} WHERE (pk1=#{pk1} AND pk2=#{pk2}) RETURNING pk1, pk2, field1, field2;",
        UpdateFactory.init(table1)
            .set(SetClause.init().all(null))
            .where(
                WhereClause.and(
                    new EqualsPlaceholder(FieldsPicker.exact(null, Arrays.asList("pk1", "pk2")))))
            .returning(new ReturningClause(FieldsPicker.all()))
            .build());
  }
}