const API_CONFIG = {
    BASE_URL: 'http://localhost:8080/api',
    ENDPOINTS: {
        GENERATE_CODE: '/v1/code-generation/generate',
        GET_STATUS: '/v1/code-generation/status/{id}',
        GET_CODE: '/v1/code-generation/code/{id}',
        GET_HEALTH: '/v1/code-generation/health',

        //пользователи
        GET_USER: '/users/{id}',
        UPDATE_USER: '/users/{id}',

        //шаблоны
        GET_TEMPLATES: '/v1/templates',
        DOWNLOAD_TEMPLATE: '/v1/templates/{id}/download',

        //проекты
        GET_PROJECTS: '/v1/projects',
        CREATE_PROJECT: '/v1/projects',
        DELETE_PROJECT: '/v1/projects/{id}'
    },
    DEFAULT_HEADERS: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
};

// дефолтный пользователь, если его не изменить самостоятельно
let currentUser = {
    id: 1,
    name: 'Developer',
    email: 'dev@example.com',
    role: 'developer'
};

async function generateCode(description, language, framework) {
    try {
        const url = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.GENERATE_CODE}`;
        console.log(`Sending request to: ${url}`);

        const response = await fetch(url, {
            method: 'POST',
            headers: {
                ...API_CONFIG.DEFAULT_HEADERS,
                'User-Id': currentUser.id.toString()
            },
            body: JSON.stringify({
                description: description,
                language: language,
                framework: framework
            })
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Server Error (${response.status}): ${errorText}`);
        }

        return await response.json();
    } catch (error) {
        console.error('Error generating code:', error);
        throw error;
    }
}

async function checkStatus(requirementId) {
    try {
        const endpoint = API_CONFIG.ENDPOINTS.GET_STATUS.replace('{id}', requirementId);
        const response = await fetch(`${API_CONFIG.BASE_URL}${endpoint}`);

        if (!response.ok) {
            throw new Error(`HTTP error status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('Error checking status:', error);
        throw error;
    }
}

// получение сгенерированного кода
async function getGeneratedCode(requirementId) {
    try {
        const endpoint = API_CONFIG.ENDPOINTS.GET_CODE.replace('{id}', requirementId);
        const response = await fetch(`${API_CONFIG.BASE_URL}${endpoint}`);

        if (!response.ok) {
            throw new Error(`HTTP error status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('Error getting generated code:', error);
        throw error;
    }
}

//отображение уведомлений
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;

    const colors = {
        success: '#10b981',
        error: '#ef4444',
        info: '#3b82f6'
    };

    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${colors[type] || colors.info};
        color: white;
        padding: 15px 20px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        display: flex;
        align-items: center;
        gap: 15px;
        z-index: 10000;
        min-width: 300px;
        font-family: 'Segoe UI', sans-serif;
    `;

    notification.innerHTML = `
        <div class="notification-content" style="display:flex; align-items:center; gap:10px;">
            <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'}"></i>
            <span>${message}</span>
        </div>
    `;

    document.body.appendChild(notification);

    setTimeout(() => {
        if (notification.parentNode) {
            notification.parentNode.removeChild(notification);
        }
    }, 4000);
}

async function getUserProfile(userId) {
    try {
        const response = await fetch(`${API_CONFIG.BASE_URL}/v1/users/${userId}`);
        if (!response.ok) throw new Error('Error loading profile');
        return await response.json();
    } catch (error) {
        console.error(error);
        throw error;
    }
}

async function updateUserProfile(userId, userData) {
    try {
        const response = await fetch(`${API_CONFIG.BASE_URL}/v1/users/${userId}`, {
            method: 'PUT',
            headers: API_CONFIG.DEFAULT_HEADERS,
            body: JSON.stringify(userData)
        });
        if (!response.ok) throw new Error('Error saving profile');
        return await response.json();
    } catch (error) {
        console.error(error);
        throw error;
    }
}
async function getTemplates() {
    try {
        const response = await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.GET_TEMPLATES}`);
        if (!response.ok) throw new Error('Error loading templates');
        return await response.json();
    } catch (error) {
        console.error(error);
        return [];
    }
}

//просто возвращает URL для скачивания, чтобы подставить его
function getDownloadUrl(templateId) {
    return `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.DOWNLOAD_TEMPLATE.replace('{id}', templateId)}`;
}

async function getUserProjects() {
    try {
        const response = await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.GET_PROJECTS}`, {
            headers: {
                ...API_CONFIG.DEFAULT_HEADERS,
                'User-Id': currentUser.id.toString()
            }
        });
        if (!response.ok) throw new Error('Error loading projects');
        return await response.json();
    } catch (error) {
        console.error(error);
        return [];
    }
}

async function createProject(projectData) {
    try {
        const response = await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.CREATE_PROJECT}`, {
            method: 'POST',
            headers: {
                ...API_CONFIG.DEFAULT_HEADERS,
                'User-Id': currentUser.id.toString()
            },
            body: JSON.stringify(projectData)
        });
        if (!response.ok) throw new Error('Error creating project');
        return await response.json();
    } catch (error) {
        throw error;
    }
}

async function deleteProject(projectId) {
    try {
        const endpoint = API_CONFIG.ENDPOINTS.DELETE_PROJECT.replace('{id}', projectId);
        const response = await fetch(`${API_CONFIG.BASE_URL}${endpoint}`, {
            method: 'DELETE',
            headers: API_CONFIG.DEFAULT_HEADERS
        });
        if (!response.ok) throw new Error('Error deleting');
        return true;
    } catch (error) {
        throw error;
    }
}

export {
    generateCode,
    checkStatus,
    getGeneratedCode,
    getUserProfile,
    updateUserProfile,
    getTemplates,
    getDownloadUrl,
    getUserProjects,
    createProject,
    deleteProject,
    showNotification,
    API_CONFIG,
    currentUser
};