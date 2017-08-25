package name.spanderman.money.transfer.microservice;

public class Withdrawal extends Deposit {

	public Withdrawal() {
		super();
	}

	public Withdrawal(int anAmount, long anAccount) {
		super(anAmount, anAccount);
	}

	public Withdrawal(long anId, int anAmount, long anAccount) {
		super(anId, anAmount, anAccount);
	}

}
