package it.vitalegi.rpgboard.be.graphql;

public class Link {

  private final String url;
  private final String description;
  private final User postedBy;

  public Link(String url, String description, User postedBy) {
    this.url = url;
    this.description = description;
    this.postedBy = postedBy;
  }

  public String getUrl() {
    return url;
  }

  public String getDescription() {
    return description;
  }

  public User getPostedBy() {
    return postedBy;
  }
}
