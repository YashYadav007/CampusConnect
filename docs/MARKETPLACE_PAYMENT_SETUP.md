# Marketplace Payment Setup

This sprint uses **Razorpay Standard Checkout in TEST MODE only**.

## 1. Create Razorpay test credentials
1. Create or log into your Razorpay account.
2. Switch to **Test Mode** in the Razorpay dashboard.
3. Go to **Account & Settings -> API Keys**.
4. Generate a test key pair.
5. Copy:
   - `Key Id`
   - `Key Secret`

## 2. Set backend environment variables
CampusConnect backend requires these variables at startup:

```bash
export RAZORPAY_KEY_ID=rzp_test_xxxxxxxxxxxx
export RAZORPAY_KEY_SECRET=your_test_secret_here
```

Windows PowerShell:

```powershell
$env:RAZORPAY_KEY_ID="rzp_test_xxxxxxxxxxxx"
$env:RAZORPAY_KEY_SECRET="your_test_secret_here"
```

## 3. Start backend
From the project root:

```bash
./mvnw spring-boot:run
```

## 4. Start frontend
From `frontend/`:

```bash
npm run dev
```

## 5. Test payment flow
1. Log in with a student account.
2. Create a marketplace listing as seller.
3. Open the listing as a different user.
4. Click **Reserve with Token Payment**.
5. Razorpay checkout will open using the backend-created order.
6. Complete the payment in test mode.
7. Frontend sends the Razorpay order id, payment id, and signature to the backend.
8. Backend verifies the signature using `RAZORPAY_KEY_SECRET`.
9. On success:
   - transaction becomes `SUCCESS`
   - item becomes `RESERVED`
   - `reservedBy` is stored

## 6. Razorpay test cards / methods
Use Razorpay's official test-mode payment methods shown in their dashboard/docs.
This sprint does **not** use live mode.

## 7. Important implementation notes
- The frontend never stores the Razorpay secret.
- The backend returns only `keyId` and order data to the frontend.
- Payment success is trusted **only after backend signature verification**.
- If verification fails, the item is not reserved.
- The verify endpoint is idempotent for already successful payments.

## 8. Current scope of this sprint
Included:
- marketplace item listing
- token payment order creation
- Razorpay test checkout
- backend signature verification
- reserve item on success
- seller mark sold

Not included:
- payouts
- escrow
- refunds
- chat
- delivery
- disputes
- live-mode payments
