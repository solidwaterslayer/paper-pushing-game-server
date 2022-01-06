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
> # All Tests And Documentation :sleepy:
>
> bank.create tests:
>
> 1. banks_can_create_accounts
> 2. an_account_has_an_account_type_like_checking_savings_or_cd_as_well_as_an_id_and_balance
> 3. a_bank_is_a_list_of_accounts
> 4. during_account_creation_banks_should_use_an_unique_8_digit_id
> 5. during_cd_creation_banks_should_use_a_balance_between_1000_and_10000_inclusive
>
> bank.timeTravel tests :mask::
>
> 1. banks_can_time_travel_between_0_and_60_months_excluding_0
> 2. the_min_balance_fee_is_100
> 3. a_low_balance_account_has_a_balance_less_than_or_equal_to_900
> 4. during_time_travel_the_bank_will_withdraw_the_min_balance_fee_from_low_balance_accounts
> 
> bank.deposit tests :laughing::
>     banks_can_deposit_to_checking_and_savings
>     banks_should_not_deposit_to_cd_accounts
>     during_deposits_banks_should_use_a_taken_id
>     during_deposits_to_checking_banks_should_use_a_deposit_amount_between_0_and_1000_excluding_0
>     during_deposits_to_savings_banks_should_use_a_deposit_amount_between_0_and_2500_excluding_0
>
> ---



banks_can_withdraw_from_accounts
    if_the_withdraw_amount_is_greater_than_the_balance_then_the_bank_will_withdraw_the_balance_instead
        during_withdraws_banks_should_use_a_taken_id
        during_withdraws_from_checking_banks_should_use_a_withdraw_amount_between_0_and_400_excluding_0
        during_withdraws_from_savings_banks_should_use_a_withdraw_amount_between_0_and_1000_excluding_0
        during_withdraws_from_cd_banks_should_use_a_withdraw_amount_greater_than_or_equal_to_the_balance
        banks_can_only_withdraw_from_a_savings_once_a_month
        banks_can_only_withdraw_from_a_cd_once_after_a_year

banks_can_transfer
    a_transfer_is_a_deposit_and_a_withdraw
    if_the_transfer_amount_is_greater_than_the_paying_account_balance_then_the_bank_will_transfer_the_paying_account_balance_instead
        during_transfers_banks_should_use_a_different_from_id_and_to_id


a_create_transaction_can_create_accounts
a_time_travel_transaction_can_time_travel
a_deposit_transaction_can_deposit_to_accounts
a_withdraw_transaction_can_withdraw_from_accounts
a_transfer_transaction_can_transfer_between_accounts

    the_first_argument_in_a_create_transaction_is_the_transaction_type_create
    the_second_argument_in_a_create_transaction_is_a_valid_account_type
    the_third_argument_in_a_create_transaction_is_a_valid_id
    the_fourth_argument_in_a_create_cd_transaction_is_a_valid_balance

    the_first_and_second_argument_in_a_time_travel_transaction_is_the_transaction_type_time_travel
    the_second_argument_in_a_time_travel_transaction_are_valid_months

    the_first_argument_in_a_deposit_transaction_is_the_transaction_type_deposit
    the_second_argument_in_a_deposit_transaction_is_a_valid_id
    the_third_argument_in_a_deposit_transaction_is_a_valid_deposit_amount

    the_first_argument_in_a_withdraw_transaction_is_the_transaction_type_withdraw
    the_second_argument_in_a_withdraw_transaction_is_a_valid_id
    the_third_argument_in_a_withdraw_transaction_is_a_valid_withdraw_amount

    the_first_argument_in_a_transfer_transaction_is_the_transaction_type_transfer
    the_second_and_third_argument_in_a_transfer_transaction_are_a_valid_paying_id_and_receiving_id
    the_fourth_argument_in_a_transfer_transaction_is_a_valid_transfer_amount


    transaction_processors_and_validators_are_handlers_in_a_chain_of_responsibility
    handlers_are_case_insensitive_and_can_ignore_extra_arguments


    a_store_inputs_a_list_of_transactions_called_an_order_and_outputs_a_receipt

    create_transactions_output_the_account_type_id_and_balance
    time_travel_transactions_do_not_have_an_output
    deposit_and_withdraw_transactions_output_themselves
    transfer_transactions_output_themselves_twice
    
    valid_transactions_output_lowercase_without_extra_arguments
    invalid_transactions_output_themselves_after_an_invalid_tag
    
    the_output_is_sorted_first_by_validity_second_by_account_third_by_time


    an_order_generator_generates_random_and_valid_transactions
    the_first_2_transactions_are_create_checking_transactions
    deposit_withdraw_and_transfer_transactions_use_amounts_divisible_by_100


    a_get_level_request_contains_a_random_order_of_size_6_its_receipt_and_transformation
    a_transformation_has_2_locations_of_receipt_mutations
    typo_mutations_remove_or_increment_1_character_while_move_mutations_swap_a_transaction_with_the_following_transaction
