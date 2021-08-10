package it.vitalegi.rpgboard.be.repository.querybuilder.pg;

import java.util.List;

public interface Renderer {
    public String render(List<TableInstance> instances);
}
