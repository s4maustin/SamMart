# D2 — Use case diagram

Actors: Buyer, Seller, Admin (seeded). Maps to F1–F8.

```mermaid
flowchart LR
    Buyer --> RegisterLogin
    Buyer --> BrowseSearch
    Buyer --> Cart
    Buyer --> Checkout
    Buyer --> OrderHistory
    Buyer --> ReviewDelivered
    Seller --> RegisterLogin
    Seller --> ManageListings
    Seller --> IncomingOrders
    Admin --> ViewUsers
    Admin --> ViewOrders
    Admin --> RemoveListings
```
