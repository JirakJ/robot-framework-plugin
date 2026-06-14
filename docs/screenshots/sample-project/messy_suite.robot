*** settings ***
Library    Collections



*** test cases ***
Add Items To Cart
	Log    Tab-indented step
	Append To List    ${items}    apple
*** keywords ***
Reset Cart
    Log    Inconsistent blank lines and lower-case headers above
