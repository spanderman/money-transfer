package name.spanderman.money.transfer.microservice;

public class Account {

	private long id;
	private int balance;
	private boolean active;

	public Account() {
		this(0);
	}

	public Account(int balance) {
		this.balance = balance;
	}

	public Account(long id, int balance, boolean active) {
		this.id = id;
		this.balance = balance;
		this.active = active;
	}

	public Account deposit(int amount) {
		balance = balance + amount;
		return this;
	}

	public Account withdraw(int amount) {
		balance = balance - amount;
		return this;
	}

	public long getId() {
		return id;
	}

	public int getBalance() {
		return balance;
	}

	public boolean isActive() {
		return active;
	}
}
