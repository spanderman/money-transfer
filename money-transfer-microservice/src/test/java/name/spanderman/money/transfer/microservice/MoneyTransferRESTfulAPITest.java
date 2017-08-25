package name.spanderman.money.transfer.microservice;

import static com.github.jsonj.tools.JsonBuilder.field;
import static com.github.jsonj.tools.JsonBuilder.object;
import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.jsonj.JsonElement;
import com.github.jsonj.tools.JsonParser;

public class MoneyTransferRESTfulAPITest extends JerseyTest {

	/**
	 * Setup in memory DB
	 * 
	 * @throws Throwable
	 */
	@BeforeClass
	public static void dbSetup() throws Throwable {
		AccountDAO.getInstance().dbSetup();
		DepositDAO.getInstance().dbSetup();
		WithdrawalDAO.getInstance().dbSetup();
		TransferDAO.getInstance().dbSetup();
	}

	@Override
	protected Application configure() {
		// Find first available port.
		forceSet(TestProperties.CONTAINER_PORT, "0");

		return new ResourceConfig().packages(Main.class.getPackage().getName());
	}

	private String openAccount(Integer balance) {
		return target("moneytransfer/accounts").request()
				.post(Entity.json(object(field("balance", balance)).toString()), String.class);
	}

	private String getAccount(Long id) {
		return target("moneytransfer/accounts").path("{id}").resolveTemplate("id", id).request().get(String.class);
	}

	private String closeAccount(Long id) {
		return target("moneytransfer/accounts").path("{id}").resolveTemplate("id", id).request().delete(String.class);
	}

	private String depositMoneyOnAccount(Integer amount, Long id) {
		return target("moneytransfer/deposits").request()
				.post(Entity.json(object(field("amount", amount), field("account", id)).toString()), String.class);
	}

	private String getDeposit(Long id) {
		return target("moneytransfer/deposits").path("{id}").resolveTemplate("id", id).request().get(String.class);
	}

	private String withdrawMoneyFromAccount(Integer amount, Long id) {
		return target("moneytransfer/withdrawals").request()
				.post(Entity.json(object(field("amount", amount), field("account", id)).toString()), String.class);
	}

	private String getWithdrawal(Long id) {
		return target("moneytransfer/withdrawals").path("{id}").resolveTemplate("id", id).request().get(String.class);
	}

	private String transferMoneyBetweenAccounts(Integer amount, Long idFrom, Long idTo) {
		return target("moneytransfer/transfers").request()
				.post(Entity.json(object(field("amount", amount), field("fromAccount", idFrom), field("toAccount", idTo)).toString()), String.class);
	}

	private String getTransfer(Long id) {
		return target("moneytransfer/transfers").path("{id}").resolveTemplate("id", id).request().get(String.class);
	}

	@Test
	public void testOpenAccount() {
		new JsonParser().parse(openAccount(123));
	}

	@Test
	public void testOpenAccountBalance() {
		Integer balance = 5000;
		JsonElement response = new JsonParser().parse(openAccount(balance));
		assertEquals(balance, response.asObject().getInt("balance"));
	}

	@Test
	public void testOpenAccountState() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		assertEquals(true, response.asObject().getBoolean("active"));
	}

	@Test
	public void testOpenAccountBadRequest() {
		Response response = target("moneytransfer/accounts").request()
				.post(Entity.json(object(field("iamnotafield", 123)).toString()));
		assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
	}

	@Test
	public void testGetAccount() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long id = response.asObject().getLong("id");
		response = new JsonParser().parse(getAccount(id));
	}

	@Test
	public void testGetAccountId() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long id = response.asObject().getLong("id");
		response = new JsonParser().parse(getAccount(id));
		assertEquals(id, response.asObject().getLong("id"));
	}

	@Test
	public void testGetAccountBalance() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long id = response.asObject().getLong("id");
		Integer balance = response.asObject().getInt("balance");
		response = new JsonParser().parse(getAccount(id));
		assertEquals(balance, response.asObject().getInt("balance"));
	}

	@Test
	public void testGetAccountState() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long id = response.asObject().getLong("id");
		Boolean active = response.asObject().getBoolean("active");
		response = new JsonParser().parse(getAccount(id));
		assertEquals(active, response.asObject().getBoolean("active"));
	}

	@Test
	public void testGetAccountBadRequest() {
		Response getResponse = target("moneytransfer/accounts").path("{id}").resolveTemplate("id", "iAmNotAnID")
				.request().get();
		assertEquals(Status.NOT_FOUND.getStatusCode(), getResponse.getStatus());
	}

	@Test
	public void testGetAccountNotFound() {
		Response getResponse = target("moneytransfer/accounts").path("{id}").resolveTemplate("id", -1).request().get();
		assertEquals(Status.NOT_FOUND.getStatusCode(), getResponse.getStatus());
	}

	@Test
	public void testCloseAccount() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long id = response.asObject().getLong("id");
		response = new JsonParser().parse(closeAccount(id));
	}

	@Test
	public void testCloseAccountId() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long id = response.asObject().getLong("id");
		response = new JsonParser().parse(closeAccount(id));
		assertEquals(id, response.asObject().getLong("id"));
	}

	@Test
	public void testCloseAccountState() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long id = response.asObject().getLong("id");
		response = new JsonParser().parse(closeAccount(id));
		assertEquals(false, response.asObject().getBoolean("active"));
	}

	@Test
	public void testCloseAccountBadRequest() {
		Response getResponse = target("moneytransfer/accounts").path("{id}").resolveTemplate("id", "iAmNotAnID")
				.request().delete();
		assertEquals(Status.NOT_FOUND.getStatusCode(), getResponse.getStatus());
	}

	@Test
	public void testCloseAccountNotFound() {
		Response getResponse = target("moneytransfer/accounts").path("{id}").resolveTemplate("id", -1).request()
				.delete();
		assertEquals(Status.NOT_FOUND.getStatusCode(), getResponse.getStatus());
	}

	@Test
	public void testDepositMoneyOnAccount() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long id = response.asObject().getLong("id");
		new JsonParser().parse(depositMoneyOnAccount(100, id));
	}

	@Test
	public void testDepositMoneyOnAccountAmount() {
		Integer amount = 5000;
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long id = response.asObject().getLong("id");
		JsonElement depositResponse = new JsonParser().parse(depositMoneyOnAccount(amount, id));
		assertEquals(amount, depositResponse.asObject().getInt("amount"));
	}

	@Test
	public void testDepositMoneyOnAccountAccount() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long id = response.asObject().getLong("id");
		response = new JsonParser().parse(depositMoneyOnAccount(100, id));
		assertEquals(id, response.asObject().getLong("account"));
	}

	@Test
	public void testDepositMoneyOnAccountAccountBalance() {
		Integer balance = 123;
		Integer amount = 333;
		JsonElement response = new JsonParser().parse(openAccount(balance));
		Long id = response.asObject().getLong("id");
		new JsonParser().parse(depositMoneyOnAccount(amount, id));
		response = new JsonParser().parse(getAccount(id));
		Integer newBalance = balance + amount;
		assertEquals(newBalance, response.asObject().getInt("balance"));
	}

	@Test
	public void testDepositMoneyOnAccountBadRequest() {
		Response depositResponse = target("moneytransfer/deposits").request()
				.post(Entity.json(object(field("iamnotafield", 100)).toString()));
		assertEquals(Status.BAD_REQUEST.getStatusCode(), depositResponse.getStatus());
	}

	@Test
	public void testDepositMoneyOnAccountAccountNotFound() {
		Response depositResponse = target("moneytransfer/deposits").request()
				.post(Entity.json(object(field("amount", 100), field("account", -1)).toString()));
		assertEquals(Status.NOT_FOUND.getStatusCode(), depositResponse.getStatus());
	}

	@Test
	public void testGetDeposit() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long id = response.asObject().getLong("id");
		response = new JsonParser().parse(depositMoneyOnAccount(100, id));
		id = response.asObject().getLong("id");
		response = new JsonParser().parse(getDeposit(id));
	}

	@Test
	public void testGetDepositId() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long id = response.asObject().getLong("id");
		response = new JsonParser().parse(depositMoneyOnAccount(100, id));
		id = response.asObject().getLong("id");
		response = new JsonParser().parse(getDeposit(id));
		assertEquals(id, response.asObject().getLong("id"));
	}

	@Test
	public void testGetDepositAmount() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long id = response.asObject().getLong("id");
		response = new JsonParser().parse(depositMoneyOnAccount(100, id));
		id = response.asObject().getLong("id");
		Integer amount = response.asObject().getInt("amount");
		response = new JsonParser().parse(getDeposit(id));
		assertEquals(amount, response.asObject().getInt("amount"));
	}

	@Test
	public void testGetDepositAccount() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long id = response.asObject().getLong("id");
		response = new JsonParser().parse(depositMoneyOnAccount(100, id));
		id = response.asObject().getLong("id");
		Long account = response.asObject().getLong("account");
		response = new JsonParser().parse(getDeposit(id));
		assertEquals(account, response.asObject().getLong("account"));
	}

	@Test
	public void testGetDepositBadRequest() {
		Response getResponse = target("moneytransfer/deposits").path("{id}").resolveTemplate("id", "iAmNotAnID")
				.request().get();
		assertEquals(Status.NOT_FOUND.getStatusCode(), getResponse.getStatus());
	}

	@Test
	public void testGetDepositNotFound() {
		Response getResponse = target("moneytransfer/deposits").path("{id}").resolveTemplate("id", -1).request().get();
		assertEquals(Status.NOT_FOUND.getStatusCode(), getResponse.getStatus());
	}

	@Test
	public void testWithdrawMoneyFromAccount() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long id = response.asObject().getLong("id");
		new JsonParser().parse(withdrawMoneyFromAccount(100, id));
	}

	@Test
	public void testWithdrawMoneyFromAccountAmount() {
		Integer amount = 5000;
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long id = response.asObject().getLong("id");
		JsonElement withdrawalResponse = new JsonParser().parse(withdrawMoneyFromAccount(amount, id));
		assertEquals(amount, withdrawalResponse.asObject().getInt("amount"));
	}

	@Test
	public void testWithdrawMoneyFromAccountAccount() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long id = response.asObject().getLong("id");
		response = new JsonParser().parse(withdrawMoneyFromAccount(100, id));
		assertEquals(id, response.asObject().getLong("account"));
	}

	@Test
	public void testWithdrawMoneyFromAccountAccountBalance() {
		Integer balance = 123;
		Integer amount = 333;
		JsonElement response = new JsonParser().parse(openAccount(balance));
		Long id = response.asObject().getLong("id");
		new JsonParser().parse(withdrawMoneyFromAccount(amount, id));
		response = new JsonParser().parse(getAccount(id));
		Integer newBalance = balance - amount;
		assertEquals(newBalance, response.asObject().getInt("balance"));
	}

	@Test
	public void testWithdrawMoneyFromAccountBadRequest() {
		Response withdrawalResponse = target("moneytransfer/withdrawals").request()
				.post(Entity.json(object(field("iamnotafield", 100)).toString()));
		assertEquals(Status.BAD_REQUEST.getStatusCode(), withdrawalResponse.getStatus());
	}

	@Test
	public void testWithdrawMoneyFromAccountAccountNotFound() {
		Response withdrawalResponse = target("moneytransfer/withdrawals").request()
				.post(Entity.json(object(field("amount", 100), field("account", -1)).toString()));
		assertEquals(Status.NOT_FOUND.getStatusCode(), withdrawalResponse.getStatus());
	}

	@Test
	public void testGetWithdrawal() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long id = response.asObject().getLong("id");
		response = new JsonParser().parse(withdrawMoneyFromAccount(100, id));
		id = response.asObject().getLong("id");
		response = new JsonParser().parse(getWithdrawal(id));
	}

	@Test
	public void testGetWithdrawalId() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long id = response.asObject().getLong("id");
		response = new JsonParser().parse(withdrawMoneyFromAccount(100, id));
		id = response.asObject().getLong("id");
		response = new JsonParser().parse(getWithdrawal(id));
		assertEquals(id, response.asObject().getLong("id"));
	}

	@Test
	public void testGetWithdrawalAmount() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long id = response.asObject().getLong("id");
		response = new JsonParser().parse(withdrawMoneyFromAccount(100, id));
		id = response.asObject().getLong("id");
		Integer amount = response.asObject().getInt("amount");
		response = new JsonParser().parse(getWithdrawal(id));
		assertEquals(amount, response.asObject().getInt("amount"));
	}

	@Test
	public void testGetWithdrawalAccount() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long id = response.asObject().getLong("id");
		response = new JsonParser().parse(withdrawMoneyFromAccount(100, id));
		id = response.asObject().getLong("id");
		Long account = response.asObject().getLong("account");
		response = new JsonParser().parse(getWithdrawal(id));
		assertEquals(account, response.asObject().getLong("account"));
	}

	@Test
	public void testGetWithdrawalBadRequest() {
		Response getResponse = target("moneytransfer/withdrawals").path("{id}").resolveTemplate("id", "iAmNotAnID")
				.request().get();
		assertEquals(Status.NOT_FOUND.getStatusCode(), getResponse.getStatus());
	}

	@Test
	public void testGetWithdrawalNotFound() {
		Response getResponse = target("moneytransfer/withdrawals").path("{id}").resolveTemplate("id", -1).request()
				.get();
		assertEquals(Status.NOT_FOUND.getStatusCode(), getResponse.getStatus());
	}

	@Test
	public void testTransferMoneyBetweenAccounts() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long idFrom = response.asObject().getLong("id");
		response = new JsonParser().parse(openAccount(456));
		Long idTo = response.asObject().getLong("id");
		new JsonParser().parse(transferMoneyBetweenAccounts(100, idFrom, idTo));
	}

	@Test
	public void testTransferMoneyBetweenAccountsAmount() {
		Integer amount = 5000;
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long idFrom = response.asObject().getLong("id");
		response = new JsonParser().parse(openAccount(456));
		Long idTo = response.asObject().getLong("id");
		JsonElement transferResponse = new JsonParser().parse(transferMoneyBetweenAccounts(amount, idFrom, idTo));
		assertEquals(amount, transferResponse.asObject().getInt("amount"));
	}

	@Test
	public void testTransferMoneyBetweenAccountsAccountFrom() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long idFrom = response.asObject().getLong("id");
		response = new JsonParser().parse(openAccount(456));
		Long idTo = response.asObject().getLong("id");
		JsonElement transferResponse = new JsonParser().parse(transferMoneyBetweenAccounts(100, idFrom, idTo));
		assertEquals(idFrom, transferResponse.asObject().getLong("fromAccount"));
	}

	@Test
	public void testTransferMoneyBetweenAccountsAccountTo() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long idFrom = response.asObject().getLong("id");
		response = new JsonParser().parse(openAccount(456));
		Long idTo = response.asObject().getLong("id");
		JsonElement transferResponse = new JsonParser().parse(transferMoneyBetweenAccounts(100, idFrom, idTo));
		assertEquals(idTo, transferResponse.asObject().getLong("toAccount"));
	}

	@Test
	public void testTransferMoneyBetweenAccountsAccountFromBalance() {
		Integer balance = 123;
		Integer amount = 333;
		JsonElement response = new JsonParser().parse(openAccount(balance));
		Long idFrom = response.asObject().getLong("id");
		response = new JsonParser().parse(openAccount(456));
		Long idTo = response.asObject().getLong("id");
		new JsonParser().parse(transferMoneyBetweenAccounts(amount, idFrom, idTo));
		response = new JsonParser().parse(getAccount(idFrom));
		Integer newBalance = balance - amount;
		assertEquals(newBalance, response.asObject().getInt("balance"));
	}

	@Test
	public void testTransferMoneyBetweenAccountsAccountToBalance() {
		Integer balance = 456;
		Integer amount = 333;
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long idFrom = response.asObject().getLong("id");
		response = new JsonParser().parse(openAccount(balance));
		Long idTo = response.asObject().getLong("id");
		new JsonParser().parse(transferMoneyBetweenAccounts(amount, idFrom, idTo));
		response = new JsonParser().parse(getAccount(idTo));
		Integer newBalance = balance + amount;
		assertEquals(newBalance, response.asObject().getInt("balance"));
	}

	@Test
	public void testTransferMoneyBetweenAccountsBadRequest() {
		Response withdrawalResponse = target("moneytransfer/transfers").request()
				.post(Entity.json(object(field("iamnotafield", 100)).toString()));
		assertEquals(Status.BAD_REQUEST.getStatusCode(), withdrawalResponse.getStatus());
	}

	@Test
	public void testTransferMoneyBetweenAccountsAccountNotFound() {
		Response withdrawalResponse = target("moneytransfer/withdrawals").request()
				.post(Entity.json(object(field("amount", 100), field("account", -1)).toString()));
		assertEquals(Status.NOT_FOUND.getStatusCode(), withdrawalResponse.getStatus());
	}

	@Test
	public void testGetTransfer() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long idFrom = response.asObject().getLong("id");
		response = new JsonParser().parse(openAccount(456));
		Long idTo = response.asObject().getLong("id");
		response = new JsonParser().parse(transferMoneyBetweenAccounts(100, idFrom, idTo));
		Long id = response.asObject().getLong("id");
		response = new JsonParser().parse(getTransfer(id));
	}

	@Test
	public void testGetTransferId() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long idFrom = response.asObject().getLong("id");
		response = new JsonParser().parse(openAccount(456));
		Long idTo = response.asObject().getLong("id");
		response = new JsonParser().parse(transferMoneyBetweenAccounts(100, idFrom, idTo));
		Long id = response.asObject().getLong("id");
		response = new JsonParser().parse(getTransfer(id));
		assertEquals(id, response.asObject().getLong("id"));
	}

	@Test
	public void testGetTransferAmount() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long idFrom = response.asObject().getLong("id");
		response = new JsonParser().parse(openAccount(456));
		Long idTo = response.asObject().getLong("id");
		response = new JsonParser().parse(transferMoneyBetweenAccounts(100, idFrom, idTo));
		Long id = response.asObject().getLong("id");
		response = new JsonParser().parse(getTransfer(id));
		Integer amount = response.asObject().getInt("amount");
		assertEquals(amount, response.asObject().getInt("amount"));
	}

	@Test
	public void testGetTransferAccountFrom() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long idFrom = response.asObject().getLong("id");
		response = new JsonParser().parse(openAccount(456));
		Long idTo = response.asObject().getLong("id");
		response = new JsonParser().parse(transferMoneyBetweenAccounts(100, idFrom, idTo));
		Long id = response.asObject().getLong("id");
		response = new JsonParser().parse(getTransfer(id));
		assertEquals(idFrom, response.asObject().getLong("fromAccount"));
	}

	@Test
	public void testGetTransferAccountTo() {
		JsonElement response = new JsonParser().parse(openAccount(123));
		Long idFrom = response.asObject().getLong("id");
		response = new JsonParser().parse(openAccount(456));
		Long idTo = response.asObject().getLong("id");
		response = new JsonParser().parse(transferMoneyBetweenAccounts(100, idFrom, idTo));
		Long id = response.asObject().getLong("id");
		response = new JsonParser().parse(getTransfer(id));
		assertEquals(idTo, response.asObject().getLong("toAccount"));
	}

	@Test
	public void testGetTransferBadRequest() {
		Response getResponse = target("moneytransfer/transfers").path("{id}").resolveTemplate("id", "iAmNotAnID")
				.request().get();
		assertEquals(Status.NOT_FOUND.getStatusCode(), getResponse.getStatus());
	}

	@Test
	public void testGetTransferNotFound() {
		Response getResponse = target("moneytransfer/transfers").path("{id}").resolveTemplate("id", -1).request()
				.get();
		assertEquals(Status.NOT_FOUND.getStatusCode(), getResponse.getStatus());
	}
}
