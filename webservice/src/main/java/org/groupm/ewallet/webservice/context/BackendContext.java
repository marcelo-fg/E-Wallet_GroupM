package org.groupm.ewallet.webservice.context;

import org.groupm.ewallet.repository.*;
import org.groupm.ewallet.repository.impl.*;
import org.groupm.ewallet.service.connector.DefaultMarketDataConnector;
import org.groupm.ewallet.service.connector.MarketDataService;

public class BackendContext {
    public static final UserRepository USER_REPO = new InMemoryUserRepository();
    public static final AccountRepository ACCOUNT_REPO = new InMemoryAccountRepository();
    public static final PortfolioRepository PORTFOLIO_REPO = new InMemoryPortfolioRepository();
    public static final TransactionRepository TRANSACTION_REPO = new InMemoryTransactionRepository();
    public static final WealthTrackerRepository WEALTH_REPO = new InMemoryWealthTrackerRepository();
    public static final AssetRepository ASSET_REPO = new InMemoryAssetRepository();

    public static final MarketDataService MARKET_DATA = new MarketDataService(new DefaultMarketDataConnector());

    private BackendContext() {}
}
