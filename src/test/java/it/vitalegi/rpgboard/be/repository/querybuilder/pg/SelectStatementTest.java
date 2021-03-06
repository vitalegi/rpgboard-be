package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import io.vertx.junit5.VertxExtension;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.fields.FieldsPicker;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.from.FromClause;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.from.Join;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.from.JoinType;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.groupby.GroupByClause;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.groupby.GroupByTableColumnValue;
import it.vitalegi.rpgboard.be.repository.querybuilder.pg.selectvalues.SelectedValues;
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
public class SelectStatementTest {
  Logger log = LoggerFactory.getLogger(this.getClass());

  Table table1 =
      TableFactory.init()
          .tableName("table1")
          .primaryKey("pk1")
          .primaryKey("pk2")
          .fields("pk1", "pk2", "field1", "field2")
          .autoGenerated("pk1")
          .build();

  Table table2 =
      TableFactory.init()
          .tableName("table2")
          .primaryKey("pk3")
          .primaryKey("pk4")
          .fields("pk3", "pk4", "field3", "field4")
          .autoGenerated("pk3")
          .build();

  @Test
  void oneTableSomeFields() {
    assertEquals(
        "SELECT t1.pk2, t1.field1, t1.field2 FROM table1 as t1;",
        SelectFactory.init(table1).values(new SelectedValues().except(null, "pk1")).build());
    assertEquals(
        "SELECT t1.pk2, t1.field1, t1.field2 FROM table1 as t1;",
        SelectFactory.init(table1)
            .values(new SelectedValues().exact(null, Arrays.asList("pk2", "field1", "field2")))
            .build());
  }

  @Test
  void oneTableNoPlaceholder() {
    assertEquals(
        "SELECT t1.pk1, t1.pk2, t1.field1, t1.field2 FROM table1 as t1;",
        SelectFactory.init(table1).build());
  }

  @Test
  void distinctTable() {
    assertEquals(
        "SELECT DISTINCT t1.pk1, t1.pk2, t1.field1, t1.field2 FROM table1 as t1;",
        SelectFactory.init(table1).values(new SelectedValues().all().distinct(true)).build());
  }

  @Test
  void oneTableOnePlaceholder() {
    assertEquals(
        "SELECT t1.pk1, t1.pk2, t1.field1, t1.field2 FROM table1 as t1 WHERE (t1.pk1=#{pk1});",
        SelectFactory.init(table1)
            .where(WhereClause.and(new EqualsPlaceholder(FieldsPicker.exact("pk1"))))
            .build());
  }

  @Test
  void oneTableTwoPlaceholder() {
    assertEquals(
        "SELECT t1.pk1, t1.pk2, t1.field1, t1.field2 FROM table1 as t1 WHERE (t1.pk1=#{pk1} AND t1.pk2=#{pk2});",
        SelectFactory.init(table1)
            .where(
                WhereClause.and(
                    new EqualsPlaceholder(FieldsPicker.exact(Arrays.asList("pk1", "pk2")))))
            .build());
  }

  @Test
  void twoTableJoin() {
    assertEquals(
        "SELECT t1.pk1, t1.pk2, t1.field1, t1.field2, t2.pk3, t2.pk4, t2.field3, t2.field4 "
            + "FROM table1 as t1 "
            + "INNER JOIN table2 as t2 ON t1.pk1=t2.pk3;",
        SelectFactory.init(table1, "t1")
            .table(table2, "t2")
            .from(new FromClause("t1").join(new Join("t2").and("t1", "pk1", "t2", "pk3")))
            .build());
  }

  @Test
  void twoTableInnerJoin() {
    assertEquals(
        "SELECT t1.pk1, t1.pk2, t1.field1, t1.field2, t2.pk3, t2.pk4, t2.field3, t2.field4 "
            + "FROM table1 as t1 "
            + "INNER JOIN table2 as t2 ON t1.pk1=t2.pk3;",
        SelectFactory.init(table1, "t1")
            .table(table2, "t2")
            .from(new FromClause("t1").join(new Join("t2").and("t1", "pk1", "t2", "pk3")))
            .build());
  }

  @Test
  void twoTableLeftJoin() {
    assertEquals(
        "SELECT t1.pk1, t1.pk2, t1.field1, t1.field2, t2.pk3, t2.pk4, t2.field3, t2.field4 "
            + "FROM table1 as t1 "
            + "LEFT JOIN table2 as t2 ON t1.pk1=t2.pk3;",
        SelectFactory.init(table1, "t1")
            .table(table2, "t2")
            .from(
                new FromClause("t1")
                    .join(new Join(JoinType.LEFT_JOIN, "t2").and("t1", "pk1", "t2", "pk3")))
            .build());
  }

  @Test
  void twoTableRightJoin() {
    assertEquals(
        "SELECT t1.pk1, t1.pk2, t1.field1, t1.field2, t2.pk3, t2.pk4, t2.field3, t2.field4 "
            + "FROM table1 as t1 "
            + "RIGHT JOIN table2 as t2 ON t1.pk1=t2.pk3;",
        SelectFactory.init(table1, "t1")
            .table(table2, "t2")
            .from(
                new FromClause("t1")
                    .join(new Join(JoinType.RIGHT_JOIN, "t2").and("t1", "pk1", "t2", "pk3")))
            .build());
  }

  @Test
  void twoTableFullOuterJoin() {
    assertEquals(
        "SELECT t1.pk1, t1.pk2, t1.field1, t1.field2, t2.pk3, t2.pk4, t2.field3, t2.field4 "
            + "FROM table1 as t1 "
            + "FULL OUTER JOIN table2 as t2 ON t1.pk1=t2.pk3;",
        SelectFactory.init(table1, "t1")
            .table(table2, "t2")
            .from(
                new FromClause("t1")
                    .join(new Join(JoinType.FULL_OUTER_JOIN, "t2").and("t1", "pk1", "t2", "pk3")))
            .build());
  }

  @Test
  void threeTableJoin() {
    assertEquals(
        "SELECT t1.pk1, t1.pk2, t2.pk3, t2.pk4, t2.field4, t3.pk3, t3.pk4, t3.field3 "
            + "FROM table1 as t1 "
            + "INNER JOIN table2 as t2 ON t1.pk1=t2.pk3 "
            + "INNER JOIN table2 as t3 ON t2.pk3=t3.pk3 AND t2.pk3=t3.pk4 "
            + "WHERE (t1.pk1=#{pk1} AND t3.pk3=#{pk3} AND t3.pk4=#{pk4} AND t3.field3=#{field3} AND t3.field4=#{field4});",
        SelectFactory.init(table1, "t1")
            .table(table2, "t2")
            .table(table2, "t3")
            .values(
                new SelectedValues()
                    .exact("t1", Arrays.asList("pk1", "pk2"))
                    .exact("t2", Arrays.asList("pk3", "pk4", "field4"))
                    .exact("t3", Arrays.asList("pk3", "pk4", "field3")))
            .from(
                new FromClause("t1")
                    .join(new Join("t2").and("t1", "pk1", "t2", "pk3"))
                    .join(
                        new Join("t3").and("t2", "pk3", "t3", "pk3").and("t2", "pk3", "t3", "pk4")))
            .where(
                WhereClause.and(
                    new EqualsPlaceholder(
                        FieldsPicker.exact("t1", Collections.singletonList("pk1"))),
                    new EqualsPlaceholder(
                        FieldsPicker.exact("t3", Arrays.asList("pk3", "pk4", "field3", "field4")))))
            .build());
  }

  @Test
  void twoTableJoinGroupBy() {
    assertEquals(
        "SELECT t1.pk1, COUNT(*) as count1, t2.pk4 "
            + "FROM table1 as t1 INNER JOIN table2 as t2 ON t1.pk1=t2.pk3 "
            + "GROUP BY t1.pk1, t2.pk4;",
        SelectFactory.init(table1, "t1")
            .table(table2, "t2")
            .values(new SelectedValues().exact("t1", "pk1").count("count1").exact("t2", "pk4"))
            .from(new FromClause("t1").join(new Join("t2").and("t1", "pk1", "t2", "pk3")))
            .groupBy(
                new GroupByClause(
                    Arrays.asList(
                        new GroupByTableColumnValue("t1", "pk1"),
                        new GroupByTableColumnValue("t2", "pk4"))))
            .build());
  }
}
