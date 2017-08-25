package name.spanderman.money.transfer.microservice;

import static org.jooq.impl.DSL.constraint;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.sequence;
import static org.jooq.impl.DSL.table;

import java.sql.Connection;
import java.sql.SQLException;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.tools.jdbc.JDBCUtils;

public class AccountDAO {

	private static final AccountDAO INSTANCE = new AccountDAO();

	private AccountDAO() {
	}

	public static AccountDAO getInstance() {
		return INSTANCE;
	}

	private Connection getConnection() throws SQLException, ClassNotFoundException {
		Class.forName("org.h2.Driver");
		return java.sql.DriverManager.getConnection("jdbc:h2:mem:money-transfer;DB_CLOSE_DELAY=-1");
	}

	private AccountDAO createAccountsTable() throws Throwable {
		Connection c = null;
		try {
			c = getConnection();
			DSLContext ctx = DSL.using(c, SQLDialect.H2);
			ctx.createTable(name("ACCOUNTS"))
					.columns(field(name("id"), SQLDataType.BIGINT), field(name("balance"), SQLDataType.INTEGER),
							field(name("active"), SQLDataType.BOOLEAN))
					.constraints(constraint("PK_ACCOUNTS").primaryKey(field(name("id")))).execute();
			return this;
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		} finally {
			JDBCUtils.safeClose(c);
		}
	}

	private AccountDAO createAccountsTableIdSequence() throws Throwable {
		Connection c = null;
		try {
			c = getConnection();
			DSLContext ctx = DSL.using(c, SQLDialect.H2);
			ctx.createSequence(sequence(name("SEQ_ACCOUNT_ID"), SQLDataType.BIGINT)).execute();
			return this;
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		} finally {
			JDBCUtils.safeClose(c);
		}
	}

	public void dbSetup() throws Throwable {
		createAccountsTable().createAccountsTableIdSequence();
	}

	public Account openAccount(Account account) throws ClassNotFoundException, SQLException {
		Connection c = null;
		try {
			c = getConnection();
			DSLContext ctx = DSL.using(c, SQLDialect.H2);
			long id = ctx.nextval(sequence(name("SEQ_ACCOUNT_ID"))).longValueExact();
			int balance = account.getBalance();
			ctx.insertInto(table(name("ACCOUNTS")), field(name("id")), field(name("balance")), field(name("active")))
					.values(id, balance, true).execute();
			return new Account(id, balance, true);
		} finally {
			JDBCUtils.safeClose(c);
		}
	}

	public Account getAccount(long id) throws ClassNotFoundException, SQLException {
		Connection c = null;
		try {
			c = getConnection();
			DSLContext ctx = DSL.using(c, SQLDialect.H2);
			Record r = ctx.select(field(name("id")), field(name("balance")), field(name("active")))
					.from(table("ACCOUNTS")).where(field(name("id"), SQLDataType.BIGINT).eq(id)).fetchOne();
			// no account found
			if (r == null) {
				return null;
			}
			return new Account(id, r.get(field(name("balance"), SQLDataType.INTEGER)), true);
		} finally {
			JDBCUtils.safeClose(c);
		}
	}

	public Account closeAccount(long id) throws SQLException, ClassNotFoundException {
		Connection c = null;
		try {
			c = getConnection();
			DSLContext ctx = DSL.using(c, SQLDialect.H2);
			return ctx.transactionResult(configuration -> {
				Record r = DSL.using(configuration).select(field(name("id")), field(name("balance")))
						.from(table("ACCOUNTS")).where(field(name("id"), SQLDataType.BIGINT).eq(id))
						.and(field(name("active"), SQLDataType.BOOLEAN)).fetchOne();
				// no open account found
				if (r == null) {
					return null;
				}
				DSL.using(configuration).update(table(name("ACCOUNTS")))
						.set(field(name("active"), SQLDataType.BOOLEAN), false)
						.where(field(name("id"), SQLDataType.BIGINT).eq(id)).execute();
				return new Account(id, r.get(field(name("balance"), SQLDataType.INTEGER)), false);
			});
		} finally {
			JDBCUtils.safeClose(c);
		}
	}

	public Deposit depositMoneyOnAccount(Deposit deposit, DSLContext ctx) {
		long account = deposit.getAccount();
		int amount = deposit.getAmount();
		return ctx.transactionResult(configuration -> {
			Record r = DSL.using(configuration).select(field(name("id")), field(name("balance")))
					.from(table("ACCOUNTS")).where(field(name("id"), SQLDataType.BIGINT).eq(account))
					.and(field(name("active"), SQLDataType.BOOLEAN)).fetchOne();
			// no open account found
			if (r == null) {
				return null;
			}
			DSL.using(configuration).update(table(name("ACCOUNTS")))
					.set(field(name("balance"), SQLDataType.INTEGER),
							r.get(field(name("balance"), SQLDataType.INTEGER)) + amount)
					.where(field(name("id"), SQLDataType.BIGINT).eq(account)).execute();
			return DepositDAO.getInstance().createDeposit(deposit, ctx);
		});
	}

	public Deposit depositMoneyOnAccount(Deposit deposit) throws ClassNotFoundException, SQLException {
		Connection c = null;
		try {
			c = getConnection();
			DSLContext ctx = DSL.using(c, SQLDialect.H2);
			return depositMoneyOnAccount(deposit, ctx);
		} finally {
			JDBCUtils.safeClose(c);
		}
	}

	public Withdrawal withdrawMoneyFromAccount(Withdrawal withdrawal, DSLContext ctx) {
		long account = withdrawal.getAccount();
		int amount = withdrawal.getAmount();
		return ctx.transactionResult(configuration -> {
			Record r = DSL.using(configuration).select(field(name("id")), field(name("balance")))
					.from(table("ACCOUNTS")).where(field(name("id"), SQLDataType.BIGINT).eq(account))
					.and(field(name("active"), SQLDataType.BOOLEAN)).fetchOne();
			// no open account found
			if (r == null) {
				return null;
			}
			DSL.using(configuration).update(table(name("ACCOUNTS")))
					.set(field(name("balance"), SQLDataType.INTEGER),
							r.get(field(name("balance"), SQLDataType.INTEGER)) - amount)
					.where(field(name("id"), SQLDataType.BIGINT).eq(account)).execute();
			return WithdrawalDAO.getInstance().createWithdrawal(withdrawal, ctx);
		});
	}

	public Withdrawal withdrawMoneyFromAccount(Withdrawal withdrawal) throws ClassNotFoundException, SQLException {
		Connection c = null;
		try {
			c = getConnection();
			DSLContext ctx = DSL.using(c, SQLDialect.H2);
			return withdrawMoneyFromAccount(withdrawal, ctx);
		} finally {
			JDBCUtils.safeClose(c);
		}
	}

	public Transfer transferMoneyBetweenAccounts(Transfer transfer) throws ClassNotFoundException, SQLException {
		Connection c = null;
		try {
			c = getConnection();
			DSLContext ctx = DSL.using(c, SQLDialect.H2);
			long fromAccount = transfer.getFromAccount();
			long toAccount = transfer.getToAccount();
			int amount = transfer.getAmount();
			return ctx.transactionResult(configuration -> {
				withdrawMoneyFromAccount(
						WithdrawalDAO.getInstance().createWithdrawal(new Withdrawal(amount, fromAccount), ctx), ctx);
				depositMoneyOnAccount(DepositDAO.getInstance().createDeposit(new Deposit(amount, toAccount), ctx), ctx);
				return TransferDAO.getInstance().createTransfer(transfer, ctx);
			});
		} finally {
			JDBCUtils.safeClose(c);
		}
	}
}
