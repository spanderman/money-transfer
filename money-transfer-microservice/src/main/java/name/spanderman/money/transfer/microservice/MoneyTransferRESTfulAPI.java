package name.spanderman.money.transfer.microservice;

import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("moneytransfer")
@Produces(MediaType.APPLICATION_JSON)
public class MoneyTransferRESTfulAPI {

	private AccountDAO accountDAO = AccountDAO.getInstance();
	private DepositDAO depositDAO = DepositDAO.getInstance();
	private WithdrawalDAO withdrawalDAO = WithdrawalDAO.getInstance();
	private TransferDAO transferDAO = TransferDAO.getInstance();

	@Path("accounts")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Object openAccount(Account account) {
		try {
			return accountDAO.openAccount(account);
		} catch (ClassNotFoundException | SQLException e) {
			Response.status(Status.INTERNAL_SERVER_ERROR);
			return new Error("error in opening account");
		}
	}

	@Path("accounts/{id}")
	@GET
	public Object getAccount(@PathParam("id") long id) {
		try {
			Account account = accountDAO.getAccount(id);
			if (account == null) {
				throw new NotFoundException("account not found");
			}
			return account;
		} catch (NotFoundException e) {
			throw e;
		} catch (Throwable e) {
			Response.status(Status.INTERNAL_SERVER_ERROR);
			return new Error("error in getting account");
		}
	}

	@Path("accounts/{id}")
	@DELETE
	public Object closeAccount(@PathParam("id") long id) {
		try {
			Account account = accountDAO.closeAccount(id);
			if (account == null) {
				throw new NotFoundException("open account not found");
			}
			return account;
		} catch (NotFoundException e) {
			throw e;
		} catch (Throwable e) {
			Response.status(Status.INTERNAL_SERVER_ERROR);
			return new Error("error in closing account");
		}
	}

	@Path("deposits")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Object depositMoneyOnAccount(Deposit deposit) {
		try {
			Deposit d = accountDAO.depositMoneyOnAccount(deposit);
			if (d == null) {
				throw new NotFoundException("open account not found");
			}
			return d;
		} catch (NotFoundException e) {
			throw e;
		} catch (Throwable e) {
			Response.status(Status.INTERNAL_SERVER_ERROR);
			return new Error("error in depositing money on account");
		}
	}

	@Path("deposits/{id}")
	@GET
	public Object getDeposit(@PathParam("id") long id) {
		try {
			Deposit deposit = depositDAO.getDeposit(id);
			if (deposit == null) {
				throw new NotFoundException("deposit not found");
			}
			return deposit;
		} catch (NotFoundException e) {
			throw e;
		} catch (Throwable e) {
			Response.status(Status.INTERNAL_SERVER_ERROR);
			return new Error("error in getting deposit");
		}
	}

	@Path("withdrawals")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Object withdrawMoneyFromAccount(Withdrawal withdrawal) {
		try {
			Withdrawal w = accountDAO.withdrawMoneyFromAccount(withdrawal);
			if (w == null) {
				throw new NotFoundException("open account not found");
			}
			return w;
		} catch (NotFoundException e) {
			throw e;
		} catch (Throwable e) {
			Response.status(Status.INTERNAL_SERVER_ERROR);
			return new Error("error in withdrawing money on account");
		}
	}

	@Path("withdrawals/{id}")
	@GET
	public Object getWithdrawal(@PathParam("id") long id) {
		try {
			Withdrawal withdrawal = withdrawalDAO.getWithdrawal(id);
			if (withdrawal == null) {
				throw new NotFoundException("withdrawal not found");
			}
			return withdrawal;
		} catch (NotFoundException e) {
			throw e;
		} catch (Throwable e) {
			Response.status(Status.INTERNAL_SERVER_ERROR);
			return new Error("error in getting withdrawal");
		}
	}

	@Path("transfers")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Object transferMoneyBetweenAccounts(Transfer transfer) {
		try {
			Transfer t = accountDAO.transferMoneyBetweenAccounts(transfer);
			if (t == null) {
				throw new NotFoundException("open accounts not found");
			}
			return t;
		} catch (NotFoundException e) {
			throw e;
		} catch (Throwable e) {
			Response.status(Status.INTERNAL_SERVER_ERROR);
			return new Error("error in transferring money between accounts");
		}
	}

	@Path("transfers/{id}")
	@GET
	public Object getTransfer(@PathParam("id") long id) {
		try {
			Transfer transfer = transferDAO.getTransfer(id);
			if (transfer == null) {
				throw new NotFoundException("transfer not found");
			}
			return transfer;
		} catch (NotFoundException e) {
			throw e;
		} catch (Throwable e) {
			Response.status(Status.INTERNAL_SERVER_ERROR);
			return new Error("error in getting transfer");
		}
	}
}
