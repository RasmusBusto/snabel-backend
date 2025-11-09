package no.snabel.resource;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.snabel.model.Account;

import java.util.List;

@Path("/api/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"USER", "ADMIN", "ACCOUNTANT"})
public class AccountResource extends SecureResource {

    @GET
    public Uni<List<Account>> listAccounts() {
        Long customerId = getCustomerId();
        return Account.<Account>find("customer.id = ?1 and active = true", customerId)
                .list();
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getAccount(@PathParam("id") Long id) {
        Long customerId = getCustomerId();
        return Account.<Account>find("id = ?1 and customer.id = ?2", id, customerId)
                .firstResult()
                .map(account -> account == null
                    ? Response.status(Response.Status.NOT_FOUND).build()
                    : Response.ok(account).build());
    }

    @POST
    @RolesAllowed({"ADMIN", "ACCOUNTANT"})
    public Uni<Response> createAccount(Account account) {
        Long customerId = getCustomerId();
        account.customer = new no.snabel.model.Customer();
        account.customer.id = customerId;

        return account.persistAndFlush()
                .map(a -> Response.status(Response.Status.CREATED).entity(a).build());
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "ACCOUNTANT"})
    public Uni<Response> updateAccount(@PathParam("id") Long id, Account updatedAccount) {
        Long customerId = getCustomerId();
        return Account.<Account>find("id = ?1 and customer.id = ?2", id, customerId)
                .firstResult()
                .chain(account -> {
                    if (account == null) {
                        return Uni.createFrom().item(Response.status(Response.Status.NOT_FOUND).build());
                    }
                    account.accountName = updatedAccount.accountName;
                    account.description = updatedAccount.description;
                    account.vatCode = updatedAccount.vatCode;
                    return account.persistAndFlush()
                            .map(a -> Response.ok(a).build());
                });
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Uni<Response> deleteAccount(@PathParam("id") Long id) {
        Long customerId = getCustomerId();
        return Account.<Account>find("id = ?1 and customer.id = ?2", id, customerId)
                .firstResult()
                .chain(account -> {
                    if (account == null) {
                        return Uni.createFrom().item(Response.status(Response.Status.NOT_FOUND).build());
                    }
                    account.active = false;
                    return account.persistAndFlush()
                            .map(a -> Response.noContent().build());
                });
    }
}
