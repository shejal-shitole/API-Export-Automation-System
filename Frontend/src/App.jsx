
import { useState, useEffect } from 'react'
import './App.css'

function App() {
  const [activePage, setActivePage] = useState('Dashboard')
  console.log("Current Active Page:", activePage)
  const [product, setProduct] = useState('')
  const [country, setCountry] = useState('')
  const [state, setState] = useState('')
  const [city, setCity] = useState('')
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)
  const [buyers, setBuyers] = useState([])
  const [editingBuyer, setEditingBuyer] = useState(null)
  const [filter, setFilter] = useState('')

  const [editForm, setEditForm] = useState({
    buyerName: '',
    companyName: '',
    email: '',
    country: '',
    website: ''
  })

  const businessCount = buyers.filter(
    buyer => buyer.buyerType === 'BUSINESS'
  ).length

  const directoryCount = buyers.filter(
    buyer => buyer.buyerType === 'DIRECTORY'
  ).length

  const individualCount = buyers.filter(
    buyer => buyer.buyerType === 'INDIVIDUAL'
  ).length

  const emailCount = buyers.filter(
    buyer => buyer.email && buyer.email.trim() !== ''
  ).length

  const filteredBuyers = buyers.filter(buyer =>
    `${buyer.buyerName || ''} ${buyer.companyName || ''} ${buyer.email || ''} ${buyer.country || ''}`
      .toLowerCase()
      .includes(filter.toLowerCase())
  )

  const fetchBuyers = async () => {
    try {
      const response = await fetch(
        'http://localhost:8080/api/buyers'
      )

      if (!response.ok) {
        throw new Error('Unable to fetch buyers')
      }

      const data = await response.json()
      setBuyers(data)

    } catch (error) {
      console.error('Buyer fetching error:', error)
      setMessage('Unable to refresh buyers.')
    }
  }

  useEffect(() => {
    fetchBuyers()
  }, [])

  const exportBuyersToCSV = () => {
    const headers = [
      'ID', 'Buyer Name', 'Company Name', 'Email',
      'Country', 'Website', 'Buyer Type', 'Source'
    ]

    const rows = filteredBuyers.map(buyer => [
      buyer.id,
      buyer.buyerName,
      buyer.companyName,
      buyer.email,
      buyer.country,
      buyer.website,
      buyer.buyerType,
      buyer.source
    ])

    const csvContent = [headers, ...rows]
      .map(row =>
        row.map(value =>
          `"${String(value ?? '').replace(/"/g, '""')}"`
        ).join(',')
      ).join('\n')

    const blob = new Blob([csvContent], {
      type: 'text/csv;charset=utf-8;'
    })

    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = 'API_Export_Buyers.csv'
    link.click()
    URL.revokeObjectURL(link.href)
  }

  const editBuyer = (buyer) => {
    setEditingBuyer(buyer.id)

    setEditForm({
      buyerName: buyer.buyerName || '',
      companyName: buyer.companyName || '',
      email: buyer.email || '',
      country: buyer.country || '',
      website: buyer.website || ''
    })
  }

  const updateBuyer = async (event) => {
    event.preventDefault()

    try {
      const response = await fetch(
        `http://localhost:8080/api/buyers/${editingBuyer}`,
        {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(editForm)
        }
      )

      if (!response.ok) {
        throw new Error('Update failed')
      }

      setEditingBuyer(null)
      setMessage('Buyer updated successfully!')
      await fetchBuyers()

    } catch (error) {
      setMessage('Unable to update buyer.')
    }
  }

  const deleteBuyer = async (id) => {
    if (!window.confirm('Are you sure you want to delete this buyer?')) {
      return
    }

    try {
      const response = await fetch(
        `http://localhost:8080/api/buyers/${id}`,
        { method: 'DELETE' }
      )

      if (!response.ok) {
        throw new Error('Delete failed')
      }

      setMessage('Buyer deleted successfully!')
      await fetchBuyers()

    } catch (error) {
      setMessage('Unable to delete buyer.')
    }
  }

  const searchBuyers = async (event) => {
    event.preventDefault()

    const query = `${product} buyers importers wholesalers ${country} ${state} ${city}`

    setLoading(true)
    setMessage('Searching for buyers...')

    try {
      const response = await fetch(
        `http://localhost:8080/api/search/discover-buyers?query=${encodeURIComponent(query)}`,
        { method: 'POST' }
      )

      if (!response.ok) {
        throw new Error('Backend request failed')
      }

      const result = await response.text()
      setMessage(result)
      await fetchBuyers()

    } catch (error) {
      setMessage('Unable to connect to Java backend.')
    } finally {
      setLoading(false)
    }
  }

  const renderBuyerTable = () => (
    <div className="table-container">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Buyer Name</th>
            <th>Product Requirement</th>
            <th>Email</th>
            <th>Website</th>
            <th>Country</th>
            <th>Buyer Type</th>
            <th>Source</th>
            <th>Action</th>
          </tr>
        </thead>

        <tbody>
          {filteredBuyers.length > 0 ? (
            filteredBuyers.map(buyer => (
              <tr key={buyer.id}>
                <td>{buyer.id}</td>
                <td>{buyer.buyerName || buyer.companyName || '—'}</td>
                <td>{buyer.productRequirement || '—'}</td>
                <td>{buyer.email || '—'}</td>
                <td>
                  {buyer.website ? (
                    <a href={buyer.website} target="_blank" rel="noreferrer">
                      Visit Website
                    </a>
                  ) : '—'}
                </td>
                <td>{buyer.country || '—'}</td>
                <td>{buyer.buyerType || '—'}</td>
                <td>{buyer.source || '—'}</td>
                <td>
                  <button onClick={() => editBuyer(buyer)}>Edit</button>
                  <button onClick={() => deleteBuyer(buyer.id)}>Delete</button>
                </td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan="9">No buyers available.</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )

  return (
   
    <div className="app">

     
<aside className="sidebar">
  <h2>API Export</h2>

  <button
    type="button"
    className={`menu ${activePage === 'Dashboard' ? 'active' : ''}`}
    onClick={() => setActivePage('Dashboard')}
  >
    Dashboard
  </button>

  <button
    type="button"
    className={`menu ${activePage === 'Find Buyers' ? 'active' : ''}`}
    onClick={() => setActivePage('Find Buyers')}
  >
    Find Buyers
  </button>

  <button
    type="button"
    className={`menu ${activePage === 'Buyer List' ? 'active' : ''}`}
    onClick={() => setActivePage('Buyer List')}
  >
    Buyer List
  </button>

  <button
    type="button"
    className={`menu ${activePage === 'Email Marketing' ? 'active' : ''}`}
    onClick={() => setActivePage('Email Marketing')}
  >
    Email Marketing
  </button>

  <button
    type="button"
    className={`menu ${activePage === 'Reports' ? 'active' : ''}`}
    onClick={() => setActivePage('Reports')}
  >
    Reports
  </button>
</aside>


      <main className="main">
        


        {activePage === 'Dashboard' && (
          <>
            <header>
              <h1>Buyer Discovery Dashboard</h1>
              <p>Find and manage international buyers</p>
            </header>

            <section className="cards">
              <div className="card">
                <h3>Total Buyers</h3>
                <h2>{buyers.length}</h2>
              </div>
              <div className="card">
                <h3>Business Buyers</h3>
                <h2>{businessCount}</h2>
              </div>
              <div className="card">
                <h3>Directory Websites</h3>
                <h2>{directoryCount}</h2>
              </div>
              <div className="card">
                <h3>Individual Buyers</h3>
                <h2>{individualCount}</h2>
              </div>
              <div className="card">
                <h3>Emails Found</h3>
                <h2>{emailCount}</h2>
              </div>
            </section>

            <section className="buyers">
              <h2>Recent Buyers</h2>
              <button onClick={exportBuyersToCSV}>Export Buyers to CSV</button>
              <button onClick={fetchBuyers}>Refresh Buyers</button>
              <input
                placeholder="Search buyers..."
                value={filter}
                onChange={e => setFilter(e.target.value)}
              />
              {renderBuyerTable()}
            </section>
          </>
        )}

        {activePage === 'Find Buyers' && (
          <section className="search-box">
            <h1>Find International Buyers</h1>

            <form onSubmit={searchBuyers}>
              <label>Product Name</label>
              <input
                value={product}
                onChange={e => setProduct(e.target.value)}
                placeholder="Example: Bamboo Handicrafts"
                required
              />

              <label>Country</label>
              <input
                value={country}
                onChange={e => setCountry(e.target.value)}
                placeholder="Example: USA"
                required
              />

              <label>State</label>
              <input
                value={state}
                onChange={e => setState(e.target.value)}
                placeholder="Example: Texas"
              />

              <label>City</label>
              <input
                value={city}
                onChange={e => setCity(e.target.value)}
                placeholder="Example: Houston"
              />

              <button type="submit" disabled={loading}>
                {loading ? 'Searching...' : 'Search Buyers'}
              </button>
            </form>

            {message && <p className="result-message">{message}</p>}
          </section>
        )}

        {activePage === 'Buyer List' && (
          <section className="buyers">
            <h1>Buyer List</h1>
            <button onClick={fetchBuyers}>Refresh Buyers</button>
            <button onClick={exportBuyersToCSV}>Export CSV</button>
            <input
              placeholder="Search by buyer, company, email or country..."
              value={filter}
              onChange={e => setFilter(e.target.value)}
            />

            {renderBuyerTable()}

            {editingBuyer !== null && (
              <section className="search-box">
                <h2>Edit Buyer</h2>

                <form onSubmit={updateBuyer}>
                  <label>Buyer Name</label>
                  <input
                    value={editForm.buyerName}
                    onChange={e => setEditForm({
                      ...editForm, buyerName: e.target.value
                    })}
                  />

                  <label>Company Name</label>
                  <input
                    value={editForm.companyName}
                    onChange={e => setEditForm({
                      ...editForm, companyName: e.target.value
                    })}
                  />

                  <label>Email</label>
                  <input
                    type="email"
                    value={editForm.email}
                    onChange={e => setEditForm({
                      ...editForm, email: e.target.value
                    })}
                  />

                  <label>Country</label>
                  <input
                    value={editForm.country}
                    onChange={e => setEditForm({
                      ...editForm, country: e.target.value
                    })}
                  />

                  <label>Website</label>
                  <input
                    value={editForm.website}
                    onChange={e => setEditForm({
                      ...editForm, website: e.target.value
                    })}
                  />

                  <button type="submit">Save Changes</button>
                  <button type="button" onClick={() => setEditingBuyer(null)}>
                    Cancel
                  </button>
                </form>
              </section>
            )}
          </section>
        )}

        {activePage === 'Email Marketing' && (
          <section className="buyers">
            <h1>Email Marketing</h1>
            <p>Manage buyer email addresses for business outreach.</p>

            <section className="cards">
              <div className="card">
                <h3>Total Buyers</h3>
                <h2>{buyers.length}</h2>
              </div>
              <div className="card">
                <h3>Emails Available</h3>
                <h2>{emailCount}</h2>
              </div>
              <div className="card">
                <h3>Emails Missing</h3>
                <h2>{buyers.length - emailCount}</h2>
              </div>
            </section>

            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Buyer Name</th>
                    <th>Company</th>
                    <th>Email</th>
                    <th>Country</th>
                    <th>Email Status</th>
                  </tr>
                </thead>
                <tbody>
                  {buyers.map(buyer => (
                    <tr key={buyer.id}>
                      <td>{buyer.buyerName || '—'}</td>
                      <td>{buyer.companyName || '—'}</td>
                      <td>{buyer.email || 'Not Available'}</td>
                      <td>{buyer.country || '—'}</td>
                      <td>{buyer.emailStatus || 'NOT_AVAILABLE'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        )}

        {activePage === 'Reports' && (
          <section className="buyers">
            <h1>Reports</h1>
            <p>Buyer discovery summary</p>

            <section className="cards">
              <div className="card">
                <h3>Total Buyers</h3>
                <h2>{buyers.length}</h2>
              </div>
              <div className="card">
                <h3>Business Buyers</h3>
                <h2>{businessCount}</h2>
              </div>
              <div className="card">
                <h3>Individual Buyers</h3>
                <h2>{individualCount}</h2>
              </div>
              <div className="card">
                <h3>Emails Found</h3>
                <h2>{emailCount}</h2>
              </div>
            </section>

            <button onClick={exportBuyersToCSV}>Download Buyer Report CSV</button>
            <button
              onClick={() => window.open(
                'http://localhost:8080/api/buyers/export-pdf',
                '_blank'
              )}
            >
              Download Buyer PDF
            </button>
          </section>
        )}

        {message && activePage !== 'Find Buyers' && (
          <p className="result-message">{message}</p>
        )}

      </main>
    </div>
  )
}

export default App
