package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import it.vitalegi.rpgboard.be.util.StringUtil;
import javafx.scene.control.Tab;

import java.util.List;
import java.util.stream.Collectors;

public class QueryTool {

  public static List<TableInstance> getAllByAlias(String alias, List<TableInstance> instances) {
    return instances.stream()
        .filter(
            instance -> {
              if (StringUtil.isNullOrEmpty(alias)) {
                return true;
              }
              return alias.equals(instance.getAlias());
            })
        .collect(Collectors.toList());
  }

  public static TableInstance getByAlias(String alias, List<TableInstance> instances) {
    List<TableInstance> found = getAllByAlias(alias, instances);
    if (found.size() != 1) {
      throw new IllegalArgumentException(
          "Expecting 1 table with alias " + alias + ". Found " + found.size());
    }
    return found.get(0);
  }

  public static String aliasDotName(String alias, String name, List<TableInstance> instances) {
    alias = getAlias(alias, instances);
    if (StringUtil.isNotNullOrEmpty(alias)) {
      return alias + "." + name;
    }
    return name;
  }

  public static String nameAsAlias(String alias, String name, List<TableInstance> instances) {
    alias = getAlias(alias, instances);
    if (StringUtil.isNotNullOrEmpty(alias)) {
      return name + " as " + alias;
    }
    return name;
  }

  public static String renderer(
      List<TableInstance> instances,
      List<? extends Renderer> renderers,
      String separator,
      String prefix,
      String suffix) {
    String rendered =
        renderers.stream()
            .map(r -> r.render(instances))
            .filter(StringUtil::isNotNullOrEmpty)
            .collect(Collectors.joining(separator));

    if (StringUtil.isNotNullOrEmpty(rendered)) {
      return prefix + rendered + suffix;
    }
    return "";
  }

  public static String getAlias(String alias, List<TableInstance> instances) {
    if (StringUtil.isNotNullOrEmpty(alias)) {
      return alias;
    }
    if (instances.size() == 1) {
      return instances.get(0).getAlias();
    }
    return null;
  }
}
