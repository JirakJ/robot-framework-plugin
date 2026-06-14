*** Settings ***
Documentation    Demo suite that intentionally contains code-quality issues so the
...              Robot Framework inspections light up for screenshots.
Library          Collections

*** Variables ***
${BASE_URL}      https://example.com
${UNUSED_TOKEN}    super-secret-value
${TIMEOUT}       10s

*** Test Cases ***
User Can Log In
    [Documentation]    Happy-path login.    # TODO: assert dashboard is shown
    Log    Opening ${BASE_URL}
    Sleep    ${TIMEOUT}

User Can Log In
    Log    Duplicate name above runs only once in Robot Framework
    Log    Trailing whitespace follows this line   

User Sees Error On Bad Password
	Log    This line is indented with a TAB, not spaces
	Log    FIXME: flaky on CI
