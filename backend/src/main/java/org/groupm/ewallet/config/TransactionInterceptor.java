package org.groupm.ewallet.config;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 * Custom transaction interceptor for RESOURCE_LOCAL transactions.
 * Since CDI @Transactional only works with JTA, this interceptor
 * provides transaction demarcation for RESOURCE_LOCAL persistence units.
 */
@Interceptor
@Transactional
@Priority(Interceptor.Priority.APPLICATION + 100)
public class TransactionInterceptor {

    @Inject
    private EntityManager em;

    @AroundInvoke
    public Object manageTransaction(InvocationContext ctx) throws Exception {
        // Check if we're already in a transaction
        boolean txStartedHere = false;

        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
            txStartedHere = true;
            System.out.println("[TX] Started transaction for: " + ctx.getMethod().getName());
        }

        try {
            Object result = ctx.proceed();

            if (txStartedHere && em.getTransaction().isActive()) {
                em.getTransaction().commit();
                System.out.println("[TX] Committed transaction for: " + ctx.getMethod().getName());
            }

            return result;
        } catch (Exception e) {
            if (txStartedHere && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                System.out.println("[TX] Rolled back transaction for: " + ctx.getMethod().getName() + " due to: "
                        + e.getMessage());
            }
            throw e;
        }
    }
}
