package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import it.vitalegi.rpgboard.be.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public abstract class AbstractPreparedStatement {

  PreparedStatementFactory factory;

  public AbstractPreparedStatement(PreparedStatementFactory factory) {
    this.factory = factory;
  }

  protected String equalToPlaceholder(String field) {
    if (StringUtil.isNullOrEmpty(factory.alias)) {
      return field + "=#{" + field + "}";
    }
    return factory.alias + "." + field + "=#{" + field + "}";
  }

  protected String placeholder(String field) {
    return "#{" + field + "}";
  }

  protected List<String> copy(List<String> from) {
    return new ArrayList<>(from);
  }

  protected Collector<CharSequence, ?, String> join(
      String delimiter, String prefix, String suffix) {
    return Collectors.joining(delimiter, prefix, suffix);
  }

  protected List<String> diff(List<String> list, List<String> toRemove) {
    return list.stream().filter(el -> !toRemove.contains(el)).collect(Collectors.toList());
  }
}
