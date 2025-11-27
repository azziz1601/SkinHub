document.addEventListener('DOMContentLoaded', () => {
    // State management
    let wardrobe = window.wardrobeData || [];
    let activeSkin = window.activeSkin || null;

    // Elements
    const wardrobeGrid = document.getElementById('wardrobe-grid');
    const uploadForm = document.getElementById('upload-form');
    const fetchForm = document.getElementById('fetch-form');
    const skinFileDevice = document.getElementById('skin-file');
    const fileNameDisplay = document.getElementById('file-name');
    const activeSkinName = document.getElementById('active-skin-name');
    const skinCountDisplay = document.getElementById('skin-count');
    const browseBtn = document.getElementById('btn-browse');

    // Initial Render
    renderWardrobe();
    renderActiveSkin();

    // --- Event Listeners ---

    // File Input Change
    skinFileDevice.addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
            fileNameDisplay.textContent = e.target.files[0].name;
            fileNameDisplay.style.color = "#2ecc71"; // Green text indicates selection
            if(browseBtn) browseBtn.textContent = "Change File";
        }
    });

    // Upload Form Submit
    uploadForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const file = skinFileDevice.files[0];
        if (!file) return showToast('Please select a file first', true);

        const formData = new FormData();
        formData.append('file', file);

        try {
            setLoading(true);
            const res = await fetch('/api/upload', {
                method: 'POST',
                body: formData
            });
            const data = await res.json();
            handleResponse(data, true);
        } catch (err) {
            showToast('Upload connection failed', true);
        } finally {
            setLoading(false);
        }
    });

    // Fetch Form Submit
    fetchForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('fetch-username').value;
        if (!username) return;

        try {
            setLoading(true);
            const res = await fetch(`/api/fetch?username=${encodeURIComponent(username)}`, {
                method: 'POST'
            });
            const data = await res.json();
            handleResponse(data, true);
        } catch (err) {
            showToast('Fetch connection failed', true);
        } finally {
            setLoading(false);
        }
    });

    // --- Helper Functions ---

    // CORE FIX: Decode Base64 Value to get Real Texture URL
    function getTextureUrl(skinData) {
        try {
            if (!skinData || !skinData.value) {
                // Fallback basic
                return 'https://minotar.net/body/MHF_Steve/150.png';
            }
            
            // 1. Decode Base64 string from DB
            const decoded = atob(skinData.value);
            // 2. Parse JSON
            const json = JSON.parse(decoded);
            
            // 3. Extract exact texture URL
            if (json.textures && json.textures.SKIN && json.textures.SKIN.url) {
                return json.textures.SKIN.url;
            }
            
            // Fallback if structure is weird
            return `https://minotar.net/body/${skinData.name}/150.png`;
        } catch (e) {
            console.error("Error parsing skin texture", e);
            // Fallback to Minotar using name if decoding fails
            return `https://minotar.net/body/${skinData.name}/150.png`;
        }
    }

    function renderActiveSkin() {
        const img = document.getElementById('current-skin-img');
        
        if (activeSkin) {
            // Gunakan texture URL asli untuk akurasi 100%
            img.src = getTextureUrl(activeSkin);
            
            // Tampilkan nama (truncate jika terlalu panjang)
            activeSkinName.textContent = activeSkin.name.length > 15 
                ? activeSkin.name.substring(0, 15) + "..." 
                : activeSkin.name;
        } else {
            activeSkinName.textContent = "None";
            img.src = "https://minotar.net/body/MHF_Steve/150.png";
        }
    }

    function renderWardrobe() {
        wardrobeGrid.innerHTML = '';
        if(skinCountDisplay) skinCountDisplay.textContent = wardrobe.length;

        wardrobe.forEach(skin => {
            const card = document.createElement('div');
            card.className = 'skin-card';
            
            // Get accurate texture
            const textureUrl = getTextureUrl(skin);

            card.innerHTML = `
                <img src="${textureUrl}" alt="${skin.name}">
                <h4>${skin.name}</h4>
                <div class="skin-actions">
                    <button class="btn-apply" onclick="applySkin(${skin.id})">Apply</button>
                    <button class="btn-delete" onclick="deleteSkin(${skin.id})">Del</button>
                </div>
            `;
            wardrobeGrid.appendChild(card);
        });
    }

    // Expose functions to window scope for HTML onclick attributes
    window.applySkin = async (id) => {
        try {
            setLoading(true);
            const res = await fetch(`/api/apply/${id}`, { method: 'POST' });
            const data = await res.json();
            handleResponse(data, true); 
        } catch (err) {
            showToast('Error applying skin', true);
            setLoading(false);
        }
    };

    window.deleteSkin = async (id) => {
        if (!confirm('Are you sure you want to delete this skin?')) return;
        try {
            setLoading(true);
            const res = await fetch(`/api/delete/${id}`, { method: 'DELETE' });
            const data = await res.json();
            handleResponse(data, true);
        } catch (err) {
            showToast('Error deleting skin', true);
            setLoading(false);
        }
    };

    function handleResponse(data, reload = false) {
        if (data.success) {
            showToast(data.message);
            if (reload) {
                setTimeout(() => location.reload(), 800);
            }
        } else {
            showToast(data.message, true);
        }
    }

    function showToast(message, isError = false) {
        const toast = document.getElementById('toast');
        toast.textContent = message;
        // Set warna manual karena CSS toast bersifat umum
        toast.style.backgroundColor = isError ? '#e74c3c' : '#2ecc71';
        toast.className = 'toast show';
        
        setTimeout(() => { 
            toast.className = toast.className.replace('show', ''); 
        }, 3000);
    }

    function setLoading(isLoading) {
        const btns = document.querySelectorAll('button');
        btns.forEach(btn => btn.disabled = isLoading);
        document.body.style.cursor = isLoading ? 'wait' : 'default';
        if(isLoading) {
            document.body.style.opacity = '0.8';
        } else {
            document.body.style.opacity = '1';
        }
    }
});