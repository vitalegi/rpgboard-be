package it.vitalegi.rpgboard.be.repository;

public class AccountRepository {

	public String add() {
		return "INSERT INTO Account (account_id, name) VALUES ($1, $2);";
	}

	public String getAll() {
		return "SELECT account_id, name FROM Account;";
	}
}
