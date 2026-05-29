// src/main/resources/static/js/main.js
function fetchPosts() {
    fetch('/api/posts')
        .then(response => {
            if (response.status === 401) {
                window.location.href = '/login';
                throw new Error('未登录');
            }
            return response.json();
        })
        .then(posts => {
            renderPosts(posts);
        })
        .catch(err => {
            if (err.message !== '未登录') {
                console.error('加载动态失败', err);
                document.getElementById('postsContainer').innerHTML = '<div class="alert alert-danger">加载失败，请刷新重试</div>';
            }
        });
}

function renderPosts(posts) {
    const container = document.getElementById('postsContainer');
    if (!posts || posts.length === 0) {
        container.innerHTML = '<div class="text-center text-muted p-5">暂无动态，发布第一条吧～</div>';
        return;
    }
    let html = '';
    posts.forEach(post => {
        const createTime = new Date(post.createTime);
        const timeStr = formatTime(createTime);
        let imageHtml = '';
        if (post.imageUrl) {
            imageHtml = `<div class="post-image"><img src="${post.imageUrl}" alt="动态图片" loading="lazy"></div>`;
        }
        let commentsHtml = '';
        if (post.comments && post.comments.length > 0) {
            post.comments.forEach(comment => {
                const commentTime = new Date(comment.createTime);
                commentsHtml += `
                    <div class="comment-item">
                        <span class="comment-author">${escapeHtml(comment.author)}</span>
                        <span class="comment-time">${formatTime(commentTime)}</span>
                        <div class="mt-1">${escapeHtml(comment.content)}</div>
                    </div>
                `;
            });
        } else {
            commentsHtml = '<div class="text-muted small">暂无评论，来抢沙发～</div>';
        }
        html += `
            <div class="post-card" data-post-id="${post.id}">
                <div class="post-header">
                    <div class="avatar" style="background-color: ${getColorFromName(post.author)}">${post.author.charAt(0).toUpperCase()}</div>
                    <div>
                        <div class="author-name">${escapeHtml(post.author)}</div>
                        <div class="post-time">${timeStr}</div>
                    </div>
                </div>
                <div class="post-content">${escapeHtml(post.content)}</div>
                ${imageHtml}
                <div class="post-actions">
                    <button class="action-btn like-btn" data-post-id="${post.id}">
                        <i class="far fa-heart"></i> 点赞 <span class="like-count">${post.likeCount || 0}</span>
                    </button>
                    <button class="action-btn comment-toggle" data-post-id="${post.id}">
                        <i class="far fa-comment"></i> 评论 ${post.comments ? post.comments.length : 0}
                    </button>
                </div>
                <div class="comment-section" id="comment-section-${post.id}">
                    <div class="comments-list" id="comments-list-${post.id}">
                        ${commentsHtml}
                    </div>
                    <div class="add-comment-form">
                        <input type="text" class="comment-content-input" id="commentContent-${post.id}" placeholder="写评论...">
                        <button class="btn btn-primary btn-sm submit-comment" data-post-id="${post.id}">发送</button>
                    </div>
                </div>
            </div>
        `;
    });
    container.innerHTML = html;
    
    document.querySelectorAll('.like-btn').forEach(btn => {
        btn.removeEventListener('click', likeHandler);
        btn.addEventListener('click', likeHandler);
    });
    document.querySelectorAll('.submit-comment').forEach(btn => {
        btn.removeEventListener('click', commentHandler);
        btn.addEventListener('click', commentHandler);
    });
}

function likeHandler(e) {
    const btn = e.currentTarget;
    const postId = btn.getAttribute('data-post-id');
    fetch('/api/like', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `postId=${postId}`
    })
    .then(res => {
        if (res.status === 401) {
            window.location.href = '/login';
            throw new Error('未登录');
        }
        return res.json();
    })
    .then(data => {
        if (data.success !== undefined) {
            fetchPosts();
        } else {
            alert('点赞失败');
        }
    })
    .catch(err => {
        if (err.message !== '未登录') alert('请求出错');
    });
}

function commentHandler(e) {
    const btn = e.currentTarget;
    const postId = btn.getAttribute('data-post-id');
    const contentInput = document.getElementById(`commentContent-${postId}`);
    const content = contentInput ? contentInput.value.trim() : '';
    if (!content) {
        alert('评论内容不能为空');
        return;
    }
    const params = new URLSearchParams();
    params.append('postId', postId);
    params.append('content', content);
    fetch('/api/comments', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    })
    .then(res => {
        if (res.status === 401) {
            window.location.href = '/login';
            throw new Error('未登录');
        }
        return res.json();
    })
    .then(data => {
        if (data.error) {
            alert(data.error);
        } else {
            if (contentInput) contentInput.value = '';
            fetchPosts();
        }
    })
    .catch(err => {
        if (err.message !== '未登录') alert('评论失败');
    });
}

// 发布动态 - 移除昵称字段
document.getElementById('submitPostBtn')?.addEventListener('click', () => {
    const content = document.getElementById('postContent').value.trim();
    const imageFile = document.getElementById('postImage').files[0];
    if (!content) {
        alert('动态内容不能为空');
        return;
    }
    const formData = new FormData();
    formData.append('content', content);
    if (imageFile) {
        formData.append('image', imageFile);
    }
    fetch('/api/posts', {
        method: 'POST',
        body: formData
    })
    .then(res => {
        if (res.status === 401) {
            window.location.href = '/login';
            throw new Error('未登录');
        }
        return res.json();
    })
    .then(data => {
        if (data.error) {
            alert(data.error);
        } else {
            const modal = document.getElementById('newPostModal');
            const closeBtn = modal.querySelector('.btn-close');
            if (closeBtn) closeBtn.click();
            document.getElementById('postContent').value = '';
            document.getElementById('postImage').value = '';
            document.getElementById('imagePreview').style.display = 'none';
            fetchPosts();
        }
    })
    .catch(err => {
        if (err.message !== '未登录') alert('发布失败: ' + err);
    });
});

// 图片预览
document.getElementById('postImage')?.addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(ev) {
            const previewDiv = document.getElementById('imagePreview');
            const previewImg = document.getElementById('previewImg');
            previewImg.src = ev.target.result;
            previewDiv.style.display = 'block';
        }
        reader.readAsDataURL(file);
    } else {
        document.getElementById('imagePreview').style.display = 'none';
    }
});

// 辅助函数
function formatTime(date) {
    if (!date) return '';
    const now = new Date();
    const diff = now - date;
    if (diff < 60 * 1000) return '刚刚';
    if (diff < 3600 * 1000) return Math.floor(diff / 60000) + '分钟前';
    if (diff < 86400 * 1000) return Math.floor(diff / 3600000) + '小时前';
    return `${date.getMonth()+1}/${date.getDate()} ${date.getHours().toString().padStart(2,'0')}:${date.getMinutes().toString().padStart(2,'0')}`;
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>]/g, function(m) {
        if (m === '&') return '&amp;';
        if (m === '<') return '&lt;';
        if (m === '>') return '&gt;';
        return m;
    });
}

function getColorFromName(name) {
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
        hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    const hue = Math.abs(hash % 360);
    return `hsl(${hue}, 70%, 55%)`;
}