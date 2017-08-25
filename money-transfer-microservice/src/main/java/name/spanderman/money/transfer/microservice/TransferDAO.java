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

public class TransferDAO {

	private static final TransferDAO INSTANCE = new TransferDAO();

	private TransferDAO() {
	}

	public static TransferDAO getInstance() {
		return INSTANCE;
	}

	private Connection getConnection() throws SQLException, ClassNotFoundException {
		Class.forName("org.h2.Driver");
		return java.sql.DriverManager.getConnection("jdbc:h2:mem:money-transfer;DB_CLOSE_DELAY=-1");
	}

	private TransferDAO createTransfersTable() throws Throwable {
		Connection c = null;
		try {
			c = getConnection();
			DSLContext ctx = DSL.using(c, SQLDialect.H2);
			ctx.createTable(name("TRANSFERS"))
					.columns(field(name("id"), SQLDataType.BIGINT), field(name("amount"), SQLDataType.INTEGER),
							field(name("fromAccount"), SQLDataType.BIGINT),
							field(name("toAccount"), SQLDataType.BIGINT))
					.constraints(constraint("PK_TRANSFERS").primaryKey(field(name("id"))),
							constraint("FK_TRANSFERS_ACCOUNTS_FROM").foreignKey(field(name("fromAccount")))
									.references(table(name("ACCOUNTS")), field(name("id"))),
							constraint("FK_TRANSFERS_ACCOUNTS_TO").foreignKey(field(name("toAccount")))
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

	private TransferDAO createTransfersTableIdSequence() throws Throwable {
		Connection c = null;
		try {
			c = getConnection();
			DSLContext ctx = DSL.using(c, SQLDialect.H2);
			ctx.createSequence(sequence(name("SEQ_TRANSFER_ID"), SQLDataType.BIGINT)).execute();
			return this;
		} catch (Throwable e) {
			e.printStackTrace();
			throw e;
		} finally {
			JDBCUtils.safeClose(c);
		}
	}

	public void dbSetup() throws Throwable {
		createTransfersTable().createTransfersTableIdSequence();
	}

	public Transfer createTransfer(Transfer transfer, DSLContext ctx) {
		long fromAccount = transfer.getFromAccount();
		long toAccount = transfer.getToAccount();
		long id = ctx.nextval(sequence(name("SEQ_TRANSFER_ID"))).longValueExact();
		int amount = transfer.getAmount();
		return ctx.transactionResult(configuration -> {
			DSL.using(configuration)
					.insertInto(table(name("TRANSFERS")), field(name("id")), field(name("amount")),
							field(name("fromAccount")), field(name("toAccount")))
					.values(id, amount, fromAccount, toAccount).execute();
			return new Transfer(id, amount, fromAccount, toAccount);
		});

	}

	public Transfer getTransfer(long id) throws ClassNotFoundException, SQLException {
		Connection c = null;
		try {
			c = getConnection();
			DSLContext ctx = DSL.using(c, SQLDialect.H2);
			Record r = ctx
					.select(field(name("id")), field(name("amount")), field(name("fromAccount")),
							field(name("toAccount")))
					.from(table("TRANSFERS")).where(field(name("id"), SQLDataType.BIGINT).eq(id)).fetchOne();
			// no transfer found
			if (r == null) {
				return null;
			}
			return new Transfer(id, r.get(field(name("amount"), SQLDataType.INTEGER)),
					r.get(field(name("fromAccount"), SQLDataType.BIGINT)),
					r.get(field(name("toAccount"), SQLDataType.BIGINT)));
		} finally {
			JDBCUtils.safeClose(c);
		}
	}
}
