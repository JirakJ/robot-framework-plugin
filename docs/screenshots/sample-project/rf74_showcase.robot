*** Settings ***
Documentation    Modern suite using the syntax of Robot Framework 7.4.
Library          OperatingSystem    AS    OS
Test Tags        smoke

*** Variables ***
${RETRIES: int}        3
${PASSWORD: Secret}    %{APP_PASSWORD}
@{BROWSERS: str}       chrome    firefox

*** Test Cases ***
Retry Login Flow
    [Tags]    login    -smoke
    GROUP    Prepare the session
        VAR    ${attempt: int}    0    scope=SUITES
    END
    WHILE    ${attempt} < ${RETRIES}    limit=10x    on_limit=PASS
        VAR    ${attempt}    ${{ $attempt + 1 }}
    END
    IF    ${attempt} > 1    Log    Retried ${attempt} times
    FOR    ${browser}    IN    @{BROWSERS}
        Log    ${browser}    ${PASSWORD}
    END

*** Keywords ***
Move ${distance: int} Meters
    [Arguments]    ${speed: float}=1.0
    [Setup]    Log    Starting the move
    TRY
        Should Be Equal    ${distance}    10
    EXCEPT    AS    ${error}
        Log    ${error}
    END
    RETURN    ${distance}
