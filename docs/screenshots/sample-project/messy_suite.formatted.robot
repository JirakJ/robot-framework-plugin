*** Settings ***
Library    Collections

*** Test Cases ***
Add Items To Cart
    Log    Tab-indented step
    Append To List    ${items}    apple

*** Keywords ***
Reset Cart
    Log    Inconsistent blank lines and lower-case headers above
