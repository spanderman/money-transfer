package name.spanderman.money.transfer.microservice;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Deposit {

	private long id;
	
	@JsonProperty(value ="",required = true)
	private int amount;
	@JsonProperty(value ="",required = true)
	private long account;

	public Deposit() {
	}

	public Deposit(int anAmount, long anAccount) {
		amount = anAmount;
		account = anAccount;
	}

	public Deposit(long anId, int anAmount, long anAccount) {
		this(anAmount, anAccount);
		id = anId;
	}

	public long getId() {
		return id;
	}

	public int getAmount() {
		return amount;
	}

	public long getAccount() {
		return account;
	}
}
