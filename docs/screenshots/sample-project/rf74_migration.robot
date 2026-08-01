*** Settings ***
Documentation    Legacy suite kept around so the Robot Framework 7.4 migration
...              inspections light up on screenshots.
Library          SeleniumLibrary    WITH NAME    Browser
Force Tags       regression

*** Variables ***
${BASE_URL}             https://example.com
${PASSWORD: Secret}     robot123
${RETRIES: list[int}    3

*** Test Cases ***
Legacy Login Flow
    [Tags]    login
    VAR    ${attempt}    scope=SUITE
    GROUP    Sign-in step
        Log    Opening ${BASE_URL}
    Log    Unclosed block above

*** Keywords ***
Get Landing Page
    [Return]    ${BASE_URL}
