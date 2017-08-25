package name.spanderman.money.transfer.microservice;

public class Transfer {

	private long id;
	private int amount;
	private long fromAccount;
	private long toAccount;

	public Transfer() {
	}

	public Transfer(int anAmount, long aFromAccount, long aToAccount) {
		amount = anAmount;
		fromAccount = aFromAccount;
		toAccount = aToAccount;
	}

	public Transfer(long anId, int anAmount, long aFromAccount, long aToAccount) {
		this(anAmount, aFromAccount, aToAccount);
		id = anId;
	}

	public long getId() {
		return id;
	}

	public int getAmount() {
		return amount;
	}

	public long getFromAccount() {
		return fromAccount;
	}

	public long getToAccount() {
		return toAccount;
	}
}
