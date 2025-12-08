import urllib.request
import json
import sys

BASE_URL = "http://localhost:8080/webservice/api"

def run_verification():
    print("Starting verification (urllib)...")
    
    user_id = "verify_user_2"
    
    # helper for requests
    def do_request(method, endpoint, data=None):
        url = f"{BASE_URL}{endpoint}"
        req = urllib.request.Request(url, method=method)
        req.add_header('Content-Type', 'application/json')
        
        jsondata = json.dumps(data).encode('utf-8') if data else None
        
        try:
            with urllib.request.urlopen(req, data=jsondata) as response:
                if response.status not in [200, 201]:
                    print(f"Error {response.status} from {url}")
                    return None
                return json.loads(response.read().decode())
        except urllib.error.HTTPError as e:
            print(f"HTTP Error {e.code}: {e.reason} body: {e.read().decode()}")
            return None
        except Exception as e:
            print(f"Error: {e}")
            return None

    # 1. Create Portfolio
    print(f"Creating portfolio for user: {user_id}")
    portfolio = do_request("POST", "/portfolios", {"userID": user_id})
    if not portfolio:
        return False
        
    portfolio_id = portfolio.get('id') or portfolio.get('portfolioID')
    print(f"Created Portfolio ID: {portfolio_id}")

    # 2. Add Asset
    asset_payload = {
        "symbol": "ETH",
        "name": "Ethereum",
        "type": "crypto",
        "quantity": 2.0,
        "unitPrice": 3000.0
    }
    
    print(f"Adding asset to portfolio {portfolio_id}...")
    added = do_request("POST", f"/portfolios/{portfolio_id}/assets", asset_payload)
    if not added:
        return False
    print(f"Added asset response: {added}")

    # 3. Fetch Assets
    print(f"Fetching assets...")
    assets = do_request("GET", f"/portfolios/{portfolio_id}/assets")
    if assets is None:
        return False
        
    for asset in assets:
        if asset.get('symbol') == "ETH":
            val = asset.get('unitValue') if 'unitValue' in asset else asset.get('unitPrice')
            print(f"Asset Found: {asset}")
            print(f"Value check: {val} vs 3000.0")
            
            if abs(float(val) - 3000.0) < 0.01:
                print("SUCCESS: Price matched!")
                return True
            else:
                print(f"FAILURE: Price mismatch.")
                return False
                
    print("FAILURE: Asset not found.")
    return False

if __name__ == "__main__":
    success = run_verification()
    sys.exit(0 if success else 1)
