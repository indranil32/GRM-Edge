
def gitCheckout() {
	stage 'Checkout GIT'
	//different ways to checkout
	//checkout from master
	//git "url: ${GIT_URL}, branch: ${GIT_BRANCH}"
	//checkout from branch hardcoding"
	//checkout from branch parameters with credentials
	//checkout from branch parameters with no credentials
	git branch: "${GIT_BRANCH}", url: "${GIT_URL}"
}
return this