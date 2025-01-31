$(() => {
    loadData();
    
    function loadData() {
		$.ajax({
		    url: '/api/data',
		    method: 'GET',
		    headers: {
		        'Accept': 'application/json'
		    },
		    success: function(response) {
		        console.log('응답:', response);
		    },
		    error: function(xhr, status, error) {
		        console.error('Error:', error);
		    }
		});
    }
});