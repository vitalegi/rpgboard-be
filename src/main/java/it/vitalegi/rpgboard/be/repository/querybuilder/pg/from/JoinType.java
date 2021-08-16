package it.vitalegi.rpgboard.be.repository.querybuilder.pg.from;

public enum JoinType {
  INNER_JOIN("INNER JOIN"),
  LEFT_JOIN("LEFT JOIN"),
  RIGHT_JOIN("RIGHT JOIN"),
  FULL_OUTER_JOIN("FULL OUTER JOIN");

  private final String statement;

  JoinType(String statement) {
    this.statement = statement;
  }

  protected String render() {
    return statement;
  }
}
