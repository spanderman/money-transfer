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

public class DepositDAO {

	private static final DepositDAO INSTANCE = new DepositDAO();

	private DepositDAO() {
	}

	public static DepositDAO getInstance() {
		return INSTANCE;
	}

	private Connection getConnection() throws SQLException, ClassNotFoundException {
		Class.forName("org.h2.Driver");
		return java.sql.DriverManager.getConnection("jdbc:h2:mem:money-transfer;DB_CLOSE_DELAY=-1");
	}

	private DepositDAO createDepositsTable() throws Throwable {
		Connection c = null;
		try {
			c = getConnection();
			DSLContext ctx = DSL.using(c, SQLDialect.H2);
			ctx.createTable(name("DEPOSITS"))
					.columns(field(name("id"), SQLDataType.BIGINT), field(name("amount"), SQLDataType.INTEGER),
							field(name("account"), SQLDataType.BIGINT))
					.constraints(constraint("PK_DEPOSITS").primaryKey(field(name("id"))),
							constraint("FK_DEPOSITS_ACCOUNTS").foreignKey(field(name("account")))
									.references(table(name("ACCOUNTS")), field(name("id"))))
					.execute();
			return this;
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		} finally {
			JDBCUtils.safeClose(c);
		}
	}

	private DepositDAO createDepositsTableIdSequence() throws Throwable {
		Connection c = null;
		try {
			c = getConnection();
			DSLContext ctx = DSL.using(c, SQLDialect.H2);
			ctx.createSequence(sequence(name("SEQ_DEPOSIT_ID"), SQLDataType.BIGINT)).execute();
			return this;
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		} finally {
			JDBCUtils.safeClose(c);
		}
	}

	public void dbSetup() throws Throwable {
		createDepositsTable().createDepositsTableIdSequence();
	}

	public Deposit createDeposit(Deposit deposit, DSLContext ctx) {
		long account = deposit.getAccount();
		long id = ctx.nextval(sequence(name("SEQ_DEPOSIT_ID"))).longValueExact();
		int amount = deposit.getAmount();
		return ctx.transactionResult(configuration -> {
			DSL.using(configuration).insertInto(table(name("DEPOSITS")), field(name("id")), field(name("amount")),
					field(name("account"))).values(id, amount, account).execute();
			return new Deposit(id, amount, account);
		});

	}

	public Deposit getDeposit(long id) throws ClassNotFoundException, SQLException {
		Connection c = null;
		try {
			c = getConnection();
			DSLContext ctx = DSL.using(c, SQLDialect.H2);
			Record r = ctx.select(field(name("id")), field(name("amount")), field(name("account")))
					.from(table("DEPOSITS")).where(field(name("id"), SQLDataType.BIGINT).eq(id)).fetchOne();
			// no deposit found
			if (r == null) {
				return null;
			}
			return new Deposit(id, r.get(field(name("amount"), SQLDataType.INTEGER)),
					r.get(field(name("account"), SQLDataType.BIGINT)));
		} finally {
			JDBCUtils.safeClose(c);
		}
	}
}
