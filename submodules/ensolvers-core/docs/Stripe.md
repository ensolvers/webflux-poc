# Stripe

Core offers a generic framework to support Stripe integration to collect payments and payouts.

The [`StripeService`](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/services/StripeService.java) class support most functionalities.

First it's good to know than Stripe has different entities to manage different roles:

- **Customer:** used to collect payments through credit cards. Only 1 card available in a moment.
- **Account:** used to apply payouts through bank accounts. Only 1 card available in a moment.

For storing that information, we can create a decorated `User` class including the following properties
- customerId
- accountId

Basic flow for both use cases are provided via [`StripeService`](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/services/StripeService.java)

## For Customer
1. Client creation can be done via `createCustomer`
2. After a client is created, a new credit card can be added via `createCardWithToken` some examples can be seen in 
    - https://github.com/ensolvers/pdl/blob/master/modules/pdl-frontend/src/components/commons/CardForm.tsx
    - https://github.com/ensolvers/pdl/blob/master/modules/pdl-frontend/src/components/Account/PaymentDetails.tsx
3. To collect a payment, we can use `createChargeForDefaultPaymentMethod` which will default with the loaded credit card 
4. A payment can be cancelled with `refundCharge`

## For Account 
1. A (bank) account can be created with `createConnectedAccount`
2. A new identity document for the owner of the account can be uploaded with `fileUpload`
3. Creation/Edition of a bank account can be done with `createBankToken` and then calling `createBankAccountWithToken`
4. A Payout can be applied via `createTransfer`


More details are provided in [`StripeServiceTest`](../modules/ensolvers-core-backend-api/src/test/java/com/ensolvers/core/common/services/StripeServiceTest.java)

## Properties needed to interact with the service
- Backend: `stripe.secure.key` (private key)
- Frontend: `environment.stripeKey` (public key)


