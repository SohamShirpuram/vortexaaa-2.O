/**
 * GovFix — 3D Visuals & Civic Cityscape
 * Uses Three.js for lightweight, tastefull 3D illustrations.
 * Includes:
 * 1. Miniature 3D Civic Cityscape in Hero (buildings, roads, trees, animated vehicles)
 * 2. Interactive 3D Sanitation Vehicle in Garbage Tracking section
 * 3. Full reduced-motion and performance optimization support
 */

const GovFix3D = {
  cityScene: null,
  cityCamera: null,
  cityRenderer: null,
  cityVehicles: [],
  cityCanvas: null,
  cityAnimationId: null,

  truckScene: null,
  truckCamera: null,
  truckRenderer: null,
  truckMesh: null,
  truckCanvas: null,
  truckAnimationId: null,

  isReducedMotion: false,

  init() {
    this.checkReducedMotion();
    this.initCityHero();
    this.initGarbageTruck();
    this.bindEvents();
  },

  checkReducedMotion() {
    const mediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
    this.isReducedMotion = mediaQuery.matches || localStorage.getItem('govfix_reduced_motion') === 'true';
  },

  setReducedMotion(enabled) {
    this.isReducedMotion = enabled;
    localStorage.setItem('govfix_reduced_motion', enabled ? 'true' : 'false');
  },

  // ==========================================
  // 1. HERO 3D CIVIC CITYSCAPE
  // ==========================================
  initCityHero() {
    this.cityCanvas = document.getElementById('hero-3d-canvas');
    if (!this.cityCanvas || typeof THREE === 'undefined') return;

    const width = this.cityCanvas.clientWidth || 500;
    const height = this.cityCanvas.clientHeight || 400;

    // Scene & Orthographic/Perspective Camera for isometric look
    this.cityScene = new THREE.Scene();
    this.cityScene.background = null; // Transparent background

    const aspect = width / height;
    this.cityCamera = new THREE.PerspectiveCamera(45, aspect, 1, 1000);
    this.cityCamera.position.set(45, 55, 65);
    this.cityCamera.lookAt(0, 0, 0);

    // Renderer
    this.cityRenderer = new THREE.WebGLRenderer({
      canvas: this.cityCanvas,
      alpha: true,
      antialias: true
    });
    this.cityRenderer.setSize(width, height);
    this.cityRenderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));

    // Lighting
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.7);
    this.cityScene.add(ambientLight);

    const dirLight = new THREE.DirectionalLight(0xffffff, 0.8);
    dirLight.position.set(50, 80, 40);
    this.cityScene.add(dirLight);

    const tealLight = new THREE.PointLight(0x0d9488, 1.2, 80);
    tealLight.position.set(0, 15, 0);
    this.cityScene.add(tealLight);

    // Build Miniature City Elements
    this.buildCityGround();
    this.buildCityBuildings();
    this.buildCityParks();
    this.buildCityVehicles();

    // Mouse parallax tracking
    let mouseX = 0, mouseY = 0;
    window.addEventListener('mousemove', (e) => {
      if (this.isReducedMotion) return;
      const rect = this.cityCanvas.getBoundingClientRect();
      if (e.clientY >= rect.top && e.clientY <= rect.bottom) {
        mouseX = (e.clientX - rect.left - width / 2) * 0.0005;
        mouseY = (e.clientY - rect.top - height / 2) * 0.0005;
      }
    });

    // Animation Loop
    const animateCity = () => {
      this.cityAnimationId = requestAnimationFrame(animateCity);

      if (!this.isReducedMotion) {
        // Move vehicles along roads
        this.cityVehicles.forEach(veh => {
          veh.mesh.position.x += veh.speed * veh.dir;
          if (veh.dir > 0 && veh.mesh.position.x > veh.limit) {
            veh.mesh.position.x = -veh.limit;
          } else if (veh.dir < 0 && veh.mesh.position.x < -veh.limit) {
            veh.mesh.position.x = veh.limit;
          }
        });

        // Gentle camera float & mouse parallax
        const time = Date.now() * 0.0008;
        this.cityCamera.position.x = 45 + Math.sin(time) * 2 + mouseX * 20;
        this.cityCamera.position.y = 55 + Math.cos(time * 0.7) * 1.5 - mouseY * 15;
        this.cityCamera.lookAt(0, 2, 0);
      }

      this.cityRenderer.render(this.cityScene, this.cityCamera);
    };

    animateCity();
  },

  buildCityGround() {
    // Base platform
    const baseGeo = new THREE.BoxGeometry(60, 2, 60);
    const baseMat = new THREE.MeshLambertMaterial({ color: 0x0f2942 }); // PCMC navy
    const base = new THREE.Mesh(baseGeo, baseMat);
    base.position.y = -1;
    this.cityScene.add(base);

    // Roads (crossroad layout)
    const roadMat = new THREE.MeshLambertMaterial({ color: 0x334155 });
    const lineMat = new THREE.MeshBasicMaterial({ color: 0xffffff });

    // Road 1 (X axis)
    const road1 = new THREE.Mesh(new THREE.BoxGeometry(60, 0.1, 10), roadMat);
    road1.position.set(0, 0.05, 0);
    this.cityScene.add(road1);

    // Road 2 (Z axis)
    const road2 = new THREE.Mesh(new THREE.BoxGeometry(10, 0.1, 60), roadMat);
    road2.position.set(0, 0.05, 0);
    this.cityScene.add(road2);

    // Center Roundabout Chowk (Pimpri Chinchwad style chowk)
    const chowkGeo = new THREE.CylinderGeometry(7, 7, 0.3, 32);
    const chowkMat = new THREE.MeshLambertMaterial({ color: 0x0d9488 }); // Teal fountain/garden
    const chowk = new THREE.Mesh(chowkGeo, chowkMat);
    chowk.position.set(0, 0.2, 0);
    this.cityScene.add(chowk);

    // Monument in center chowk
    const monumentGeo = new THREE.CylinderGeometry(0.8, 1.2, 6, 16);
    const monumentMat = new THREE.MeshLambertMaterial({ color: 0xffffff });
    const monument = new THREE.Mesh(monumentGeo, monumentMat);
    monument.position.set(0, 3.2, 0);
    this.cityScene.add(monument);
  },

  buildCityBuildings() {
    const buildingColors = [0x1e3a5f, 0x0f766e, 0x38bdf8, 0xffffff, 0xf1f5f9];
    
    // Array of building specs [x, z, width, height, depth, colorIdx]
    const buildings = [
      // Top-Left Quadrant (City Administration)
      [-18, -18, 10, 18, 10, 0],
      [-8, -20, 6, 12, 6, 2],
      [-20, -8, 7, 10, 7, 3],

      // Top-Right Quadrant (IT & Commercial - Hinjawadi/Wakad)
      [18, -18, 9, 22, 9, 1],
      [8, -20, 6, 14, 6, 3],
      [20, -8, 8, 16, 7, 0],

      // Bottom-Left Quadrant (Residential - Pradhikaran/Nigdi)
      [-18, 18, 8, 14, 8, 3],
      [-8, 20, 7, 9, 7, 4],
      [-20, 8, 6, 11, 6, 1],

      // Bottom-Right Quadrant (Industrial / MIDC - Bhosari)
      [18, 18, 11, 10, 11, 0],
      [8, 20, 6, 8, 6, 3],
      [20, 8, 7, 13, 7, 2]
    ];

    buildings.forEach(([x, z, w, h, d, colIdx]) => {
      const geo = new THREE.BoxGeometry(w, h, d);
      const mat = new THREE.MeshLambertMaterial({ color: buildingColors[colIdx] });
      const mesh = new THREE.Mesh(geo, mat);
      mesh.position.set(x, h / 2, z);

      // Roof trim
      const roofGeo = new THREE.BoxGeometry(w + 0.4, 0.4, d + 0.4);
      const roofMat = new THREE.MeshLambertMaterial({ color: 0x0f2942 });
      const roof = new THREE.Mesh(roofGeo, roofMat);
      roof.position.set(x, h + 0.2, z);

      this.cityScene.add(mesh);
      this.cityScene.add(roof);
    });
  },

  buildCityParks() {
    // Trees in city corners and chowk
    const treeCoords = [
      [-5, -12], [-12, -5], [5, -12], [12, -5],
      [-5, 12], [-12, 5], [5, 12], [12, 5],
      [0, 2], [-2, 0], [2, 0], [0, -2]
    ];

    const trunkMat = new THREE.MeshLambertMaterial({ color: 0x78350f });
    const foliageMat = new THREE.MeshLambertMaterial({ color: 0x15803d });

    treeCoords.forEach(([tx, tz]) => {
      // Trunk
      const trunk = new THREE.Mesh(new THREE.CylinderGeometry(0.3, 0.4, 2, 8), trunkMat);
      trunk.position.set(tx, 1, tz);
      // Foliage
      const foliage = new THREE.Mesh(new THREE.ConeGeometry(1.6, 3.5, 8), foliageMat);
      foliage.position.set(tx, 3, tz);

      this.cityScene.add(trunk);
      this.cityScene.add(foliage);
    });
  },

  buildCityVehicles() {
    // Miniature moving municipal vehicles along road
    const carColors = [0xff9933, 0x14b8a6, 0xffffff, 0xef4444];

    // Vehicle 1: Eastbound car on Road 1 (Z = 2.5)
    this.createCityCar(-25, 2.5, 0.25, 1, 0xff9933, 28);
    // Vehicle 2: Westbound bus on Road 1 (Z = -2.5)
    this.createCityBus(20, -2.5, -0.18, -1, 0x0284c7, 28);
    // Vehicle 3: Northbound truck on Road 2 (X = 2.5)
    this.createCityCar(-15, 2.5, 0.22, 1, 0x10b981, 28, true);
  },

  createCityCar(x, z, speed, dir, color, limit, isRotated = false) {
    const group = new THREE.Group();
    const body = new THREE.Mesh(new THREE.BoxGeometry(3, 1.2, 1.6), new THREE.MeshLambertMaterial({ color }));
    body.position.y = 0.6;
    const roof = new THREE.Mesh(new THREE.BoxGeometry(1.6, 0.8, 1.3), new THREE.MeshLambertMaterial({ color: 0xffffff }));
    roof.position.set(-0.2, 1.4, 0);

    group.add(body);
    group.add(roof);
    group.position.set(x, 0.2, z);
    if (isRotated) group.rotation.y = Math.PI / 2;

    this.cityScene.add(group);
    this.cityVehicles.push({ mesh: group, speed, dir, limit });
  },

  createCityBus(x, z, speed, dir, color, limit) {
    const group = new THREE.Group();
    const body = new THREE.Mesh(new THREE.BoxGeometry(5.5, 1.8, 1.8), new THREE.MeshLambertMaterial({ color }));
    body.position.y = 1.0;
    const windows = new THREE.Mesh(new THREE.BoxGeometry(4.5, 0.6, 1.85), new THREE.MeshLambertMaterial({ color: 0x0f2942 }));
    windows.position.set(0, 1.3, 0);

    group.add(body);
    group.add(windows);
    group.position.set(x, 0.2, z);

    this.cityScene.add(group);
    this.cityVehicles.push({ mesh: group, speed, dir, limit });
  },

  // ==========================================
  // 2. 3D SANITATION GARBAGE TRUCK (FEATURED)
  // ==========================================
  initGarbageTruck() {
    this.truckCanvas = document.getElementById('truck-3d-canvas');
    if (!this.truckCanvas || typeof THREE === 'undefined') return;

    const width = this.truckCanvas.clientWidth || 320;
    const height = this.truckCanvas.clientHeight || 240;

    this.truckScene = new THREE.Scene();
    this.truckScene.background = null;

    this.truckCamera = new THREE.PerspectiveCamera(40, width / height, 0.1, 100);
    this.truckCamera.position.set(12, 9, 14);
    this.truckCamera.lookAt(0, 1, 0);

    this.truckRenderer = new THREE.WebGLRenderer({
      canvas: this.truckCanvas,
      alpha: true,
      antialias: true
    });
    this.truckRenderer.setSize(width, height);
    this.truckRenderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));

    // Lights
    const ambient = new THREE.AmbientLight(0xffffff, 0.8);
    this.truckScene.add(ambient);

    const dirLight = new THREE.DirectionalLight(0xffffff, 1.0);
    dirLight.position.set(10, 15, 10);
    this.truckScene.add(dirLight);

    // Build Detailed Low-Poly Garbage Collection Truck
    this.truckMesh = new THREE.Group();

    // Chassis
    const chassis = new THREE.Mesh(
      new THREE.BoxGeometry(8, 0.5, 2.8),
      new THREE.MeshLambertMaterial({ color: 0x334155 })
    );
    chassis.position.y = 1;
    this.truckMesh.add(chassis);

    // Driver Cab (White with blue windshield)
    const cab = new THREE.Mesh(
      new THREE.BoxGeometry(2.4, 2.6, 2.7),
      new THREE.MeshLambertMaterial({ color: 0xffffff })
    );
    cab.position.set(2.6, 2.4, 0);
    this.truckMesh.add(cab);

    // Windshield
    const glass = new THREE.Mesh(
      new THREE.BoxGeometry(0.8, 1.2, 2.6),
      new THREE.MeshLambertMaterial({ color: 0x0284c7 })
    );
    glass.position.set(3.5, 2.8, 0);
    this.truckMesh.add(glass);

    // Sanitation Compactor Container Body (PCMC Teal/Green)
    const body = new THREE.Mesh(
      new THREE.BoxGeometry(5.2, 3.2, 2.8),
      new THREE.MeshLambertMaterial({ color: 0x0d9488 })
    );
    body.position.set(-1.4, 2.8, 0);
    this.truckMesh.add(body);

    // Rear Hopper / Lifter
    const hopper = new THREE.Mesh(
      new THREE.BoxGeometry(1.2, 2.4, 2.6),
      new THREE.MeshLambertMaterial({ color: 0x0f766e })
    );
    hopper.position.set(-4.4, 2.2, 0);
    this.truckMesh.add(hopper);

    // Rooftop Emergency / Warning Beacon Light (Saffron Orange)
    const beacon = new THREE.Mesh(
      new THREE.CylinderGeometry(0.3, 0.3, 0.4, 12),
      new THREE.MeshLambertMaterial({ color: 0xff9933 })
    );
    beacon.position.set(2.4, 3.9, 0);
    this.truckMesh.add(beacon);

    // Wheels (4 pairs)
    const wheelMat = new THREE.MeshLambertMaterial({ color: 0x1e293b });
    const wheelGeo = new THREE.CylinderGeometry(0.7, 0.7, 0.6, 16);

    const wheelPos = [
      [2.5, 0.7, 1.4], [2.5, 0.7, -1.4],
      [-1.0, 0.7, 1.4], [-1.0, 0.7, -1.4],
      [-2.8, 0.7, 1.4], [-2.8, 0.7, -1.4]
    ];

    wheelPos.forEach(([wx, wy, wz]) => {
      const wheel = new THREE.Mesh(wheelGeo, wheelMat);
      wheel.rotation.x = Math.PI / 2;
      wheel.position.set(wx, wy, wz);
      this.truckMesh.add(wheel);
    });

    // Circular pedestal base
    const pedestal = new THREE.Mesh(
      new THREE.CylinderGeometry(6, 6.4, 0.4, 32),
      new THREE.MeshLambertMaterial({ color: 0x0f2942 })
    );
    pedestal.position.y = -0.2;
    this.truckScene.add(pedestal);

    this.truckScene.add(this.truckMesh);

    // Animation loop: subtle 360 rotation
    const animateTruck = () => {
      this.truckAnimationId = requestAnimationFrame(animateTruck);
      if (!this.isReducedMotion && this.truckMesh) {
        this.truckMesh.rotation.y += 0.008;
      }
      this.truckRenderer.render(this.truckScene, this.truckCamera);
    };

    animateTruck();
  },

  bindEvents() {
    window.addEventListener('resize', () => {
      // Resize hero 3D
      if (this.cityCanvas && this.cityRenderer && this.cityCamera) {
        const w = this.cityCanvas.clientWidth;
        const h = this.cityCanvas.clientHeight;
        if (w && h) {
          this.cityCamera.aspect = w / h;
          this.cityCamera.updateProjectionMatrix();
          this.cityRenderer.setSize(w, h);
        }
      }

      // Resize truck 3D
      if (this.truckCanvas && this.truckRenderer && this.truckCamera) {
        const tw = this.truckCanvas.clientWidth;
        const th = this.truckCanvas.clientHeight;
        if (tw && th) {
          this.truckCamera.aspect = tw / th;
          this.truckCamera.updateProjectionMatrix();
          this.truckRenderer.setSize(tw, th);
        }
      }
    });
  }
};

window.GovFix3D = GovFix3D;

