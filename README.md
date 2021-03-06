<br>

> ---
>
> # Main Project And README :hugs:
> 
> Here is a main repository of the project w/ README:
> 
> https://github.com/solidwaterslayer/paper-pushing-game-client
>
> ---

<br>
<br>

> ---
>
> # All Tests And Documentation :blush:
>
> Bank.Create Tests:
>
    banks_can_create_accounts
    an_account_has_an_account_type_like_checking_savings_or_cd_as_well_as_an_id_and_balance
    a_bank_is_a_list_of_accounts
    during_account_creation_banks_should_use_an_unique_8_digit_id
    during_cd_creation_banks_should_use_a_balance_between_1000_and_10000_inclusive
>
> Bank.TimeTravel Tests:
>
    banks_can_time_travel_between_0_and_60_months_excluding_0
    the_min_balance_fee_is_100
    a_low_balance_account_has_a_balance_less_than_or_equal_to_900
    during_time_travel_the_bank_will_withdraw_the_min_balance_fee_from_low_balance_accounts
> 
> Bank.Deposit Tests:
>
    banks_can_deposit_to_checking_and_savings
    banks_should_not_deposit_to_cd_accounts
    during_deposits_banks_should_use_a_taken_id
    during_deposits_to_checking_banks_should_use_a_deposit_amount_between_0_and_1000_excluding_0
    during_deposits_to_savings_banks_should_use_a_deposit_amount_between_0_and_2500_excluding_0
>
> Bank.Withdraw Tests:ocean::
>
    banks_can_withdraw_from_accounts
    if_the_withdraw_amount_is_greater_than_the_balance_then_the_bank_will_withdraw_the_balance_instead
    during_withdraws_banks_should_use_a_taken_id
    during_withdraws_from_checking_banks_should_use_a_withdraw_amount_between_0_and_400_excluding_0
    during_withdraws_from_savings_banks_should_use_a_withdraw_amount_between_0_and_1000_excluding_0
    during_withdraws_from_cd_banks_should_use_a_withdraw_amount_greater_than_or_equal_to_the_balance
    banks_can_only_withdraw_from_a_savings_once_a_month
    banks_can_only_withdraw_from_a_cd_once_after_a_year
>
> Bank.Transfer Tests:
>
    banks_can_transfer
    a_transfer_is_a_deposit_and_a_withdraw
    if_the_transfer_amount_is_greater_than_the_paying_account_balance_then_the_bank_will_transfer_the_paying_account_balance_instead
    during_transfers_banks_should_use_a_different_from_id_and_to_id
>
> Create Transaction Tests:
>
    a_create_transaction_can_create_accounts
    the_first_argument_in_a_create_transaction_is_the_transaction_type_create
    the_second_argument_in_a_create_transaction_is_a_valid_account_type
    the_third_argument_in_a_create_transaction_is_a_valid_id
    the_fourth_argument_in_a_create_cd_transaction_is_a_valid_balance
>
> Time Travel Transaction Tests:
>
    a_time_travel_transaction_can_time_travel
    the_first_and_second_argument_in_a_time_travel_transaction_is_the_transaction_type_time_travel
    the_second_argument_in_a_time_travel_transaction_are_valid_months
>
> Deposit Transaction Tests :snowflake::
>
    a_deposit_transaction_can_deposit_to_accounts
    the_first_argument_in_a_deposit_transaction_is_the_transaction_type_deposit
    the_second_argument_in_a_deposit_transaction_is_a_valid_id
    the_third_argument_in_a_deposit_transaction_is_a_valid_deposit_amount
>
> Withdraw Transaction Tests:
>
    a_withdraw_transaction_can_withdraw_from_accounts
    the_first_argument_in_a_withdraw_transaction_is_the_transaction_type_withdraw
    the_second_argument_in_a_withdraw_transaction_is_a_valid_id
    the_third_argument_in_a_withdraw_transaction_is_a_valid_withdraw_amount
>
> Transfer Transaction Tests:
>
    a_transfer_transaction_can_transfer_between_accounts
    the_first_argument_in_a_transfer_transaction_is_the_transaction_type_transfer
    the_second_and_third_argument_in_a_transfer_transaction_are_a_valid_paying_id_and_receiving_id
    the_fourth_argument_in_a_transfer_transaction_is_a_valid_transfer_amount
>
> Handler Tests:
>
    transaction_processors_and_validators_are_handlers_in_a_chain_of_responsibility
    handlers_are_case_insensitive_and_can_ignore_extra_arguments
>
> Store Tests :wave::
>
    a_store_inputs_a_list_of_transactions_called_an_order_and_outputs_a_receipt
    create_transactions_output_the_account_type_id_and_balance
    time_travel_transactions_do_not_have_an_output
    deposit_and_withdraw_transactions_output_themselves
    transfer_transactions_output_themselves_twice 

    valid_transactions_output_lowercase_without_extra_arguments
    invalid_transactions_output_themselves_after_an_invalid_tag
    the_output_is_sorted_first_by_validity_second_by_account_third_by_time
>
> Generator Tests:
>
    an_order_generator_generates_random_and_valid_transactions
    the_first_2_transactions_are_create_checking_transactions
    deposit_withdraw_and_transfer_transactions_use_amounts_divisible_by_100
>
> Level Tests :stuck_out_tongue_winking_eye::
>
    a_get_level_request_contains_a_random_order_of_size_6_its_receipt_and_transformation
    a_transformation_has_2_locations_of_receipt_mutations
    typo_mutations_remove_or_increment_1_character_while_move_mutations_swap_a_transaction_with_the_following_transaction
>
> ---
